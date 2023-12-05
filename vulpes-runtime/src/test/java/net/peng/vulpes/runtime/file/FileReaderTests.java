package net.peng.vulpes.runtime.file;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.runtime.file.FileReader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import org.apache.arrow.dataset.file.FileFormat;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.junit.Test;

/**
 * Description of FileReaderTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/13
 */
public class FileReaderTests {

  @Test
  public void csvReaderTest() throws Exception {
    Properties properties = new Properties();
    properties.put(ConfigItems.FILE_READ_ROW_BATCH_SIZE.name(), 2L);
    FileReader fileReader =
            new FileReader(ImmutableList.of("file://" + this.getClass().getClassLoader()
                    .getResource("data/table1.csv").getFile()), FileFormat.CSV,
                    new Config(properties));
    BufferAllocator allocator = new RootAllocator();
    List<VectorSchemaRoot> result = ((ArrowSegment) fileReader
            .fetch(MemorySpace.builder().allocator(allocator).build())).get();
    System.out.println(result.stream().map(VectorSchemaRoot::contentToTSVString).toList());
    AutoCloseables.close(result);
    allocator.close();
  }

  @Test
  public void parquetReaderTest() throws Exception {
    Properties properties = new Properties();
    properties.put(ConfigItems.FILE_READ_ROW_BATCH_SIZE.name(), 1000L);
    FileReader fileReader =
            new FileReader(ImmutableList.of("file://" + this.getClass().getClassLoader()
                    .getResource("data/supplier.parquet").getFile()), FileFormat.PARQUET,
                    new Config(properties));
    BufferAllocator allocator = new RootAllocator();
    List<VectorSchemaRoot> result = ((ArrowSegment) fileReader
            .fetch(MemorySpace.builder().allocator(allocator).build())).get();
    System.out.println(result.stream().map(VectorSchemaRoot::contentToTSVString).toList());
    AutoCloseables.close(result);
    allocator.close();
  }
}
