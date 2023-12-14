package net.peng.vulpes.runtime.file;

import java.util.ArrayList;
import java.util.List;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.exception.DataFetchException;
import net.peng.vulpes.runtime.DataFetcher;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.dataset.file.FileFormat;
import org.apache.arrow.dataset.file.FileSystemDatasetFactory;
import org.apache.arrow.dataset.jni.NativeMemoryPool;
import org.apache.arrow.dataset.scanner.ScanOptions;
import org.apache.arrow.dataset.scanner.Scanner;
import org.apache.arrow.dataset.source.Dataset;
import org.apache.arrow.dataset.source.DatasetFactory;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.VectorLoader;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.VectorUnloader;
import org.apache.arrow.vector.ipc.ArrowReader;
import org.apache.arrow.vector.ipc.message.ArrowRecordBatch;
import org.apache.arrow.vector.types.pojo.Schema;

/**
 * Description of FileReader.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/13
 */
@Slf4j
@ToString
public class FileReader implements DataFetcher {

  private final List<String> paths;

  private final FileFormat fileFormat;

  private final Config config;

  /**
   * 文件读取方法类.
   */
  public FileReader(List<String> paths, FileFormat fileFormat, Config config) {
    this.paths = paths;
    this.fileFormat = fileFormat;
    this.config = config;
  }

  /**
   * 获取数据接口.
   */
  public Segment<?> fetch(MemorySpace memorySpace) {
    List<VectorSchemaRoot> rootBatches = new ArrayList<>();
    ScanOptions scanOptions = new ScanOptions(config.get(ConfigItems.FILE_READ_ROW_BATCH_SIZE));
    for (String path : paths) {
      rootBatches.addAll(internalFetch(path, memorySpace.getAllocator(), scanOptions));
    }
    return new ArrowSegment(rootBatches);
  }

  private List<VectorSchemaRoot> internalFetch(String path, BufferAllocator allocator,
                                               ScanOptions scanOptions) {
    List<VectorSchemaRoot> rootBatches = new ArrayList<>();
    List<ArrowRecordBatch> recordBatches = new ArrayList<>();
    Schema schema = null;
    try (DatasetFactory datasetFactory = new FileSystemDatasetFactory(
            allocator, NativeMemoryPool.getDefault(),
            fileFormat, path);
         Dataset dataset = datasetFactory.finish();
         Scanner scanner = dataset.newScan(scanOptions);
         ArrowReader reader = scanner.scanBatches()) {
      while (reader.loadNextBatch()) {
        VectorSchemaRoot root = reader.getVectorSchemaRoot();
        if (schema == null) {
          schema = root.getSchema();
        }
        final VectorUnloader unloader = new VectorUnloader(root);
        final ArrowRecordBatch arrowRecordBatch = unloader.getRecordBatch();
        recordBatches.add(arrowRecordBatch);
        VectorSchemaRoot vectorSchemaRoot = VectorSchemaRoot.create(schema, allocator);
        final VectorLoader loader = new VectorLoader(vectorSchemaRoot);
        loader.load(arrowRecordBatch);
        rootBatches.add(vectorSchemaRoot);
      }
    } catch (Exception e) {
      try {
        AutoCloseables.close(rootBatches);
      } catch (Exception ex) {
        throw new DataFetchException("读取文件错误, 关闭异常.", e);
      }
      throw new DataFetchException("读取文件错误[%s]", e.getMessage(), e);
    } finally {
      try {
        AutoCloseables.close(recordBatches);
      } catch (Exception e) {
        log.error("关闭 ArrowRecordBatch 出错:", e);
      }
    }
    return rootBatches;
  }
}
