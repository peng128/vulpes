package net.peng.vulpes.catalog.file.arrow;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.embedded.EmbeddedTableMeta;
import net.peng.vulpes.catalog.file.arrow.utils.TypeConvertor;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.configuration.FileHelper;
import net.peng.vulpes.common.exception.TableException;
import net.peng.vulpes.common.model.DataFormat;
import net.peng.vulpes.common.model.TableIdentifier;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.utils.ObjectUtils;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.SeekableReadChannel;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;

/**
 * Description of FileParquetCatalog.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/11
 */
public class FileArrowCatalog implements Catalog {

  private final String catalogPath;

  private static final String SUFFIX = ".arrow";

  public FileArrowCatalog(String catalogPath) {
    this.catalogPath = catalogPath;
  }

  @Override
  public TableMeta getTable(TableIdentifier tableIdentifier) {
    if (ObjectUtils.isNull(tableIdentifier.getSchema())) {
      throw new TableException("[%s]Schema 是空，读不出表.", tableIdentifier);
    }
    String filePath = String.format("%s/%s/%s%s", catalogPath, tableIdentifier.getSchema(),
        tableIdentifier.getTable(), SUFFIX);
    try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath));
         SeekableReadChannel seekableReadChannel = new SeekableReadChannel(fileChannel);
         ArrowFileReader fileReader = new ArrowFileReader(seekableReadChannel,
             new RootAllocator(Long.MAX_VALUE));
         VectorSchemaRoot schemaRoot = fileReader.getVectorSchemaRoot()) {

      Schema schema = schemaRoot.getSchema();

      List<String> fieldNames = new ArrayList<>(schema.getFields().size());
      List<DataType> dataTypes = new ArrayList<>(schema.getFields().size());
      // 遍历每个字段
      for (Field field : schema.getFields()) {
        fieldNames.add(field.getName());
        dataTypes.add(TypeConvertor.convert(field.getFieldType()));
      }
      return new EmbeddedTableMeta(fieldNames, dataTypes,
          List.of(String.format("%s/%s/%s%s", tableIdentifier.getCatalog(),
              tableIdentifier.getSchema(), tableIdentifier.getTable(), SUFFIX)),
          DataFormat.ARROW_IPC);
    } catch (IOException e) {
      throw new TableException("表错误", e);
    }
  }

  @Override
  public List<String> getTableNames(String schema) {
    return FileHelper.listSubNames(String.format("%s/%s", catalogPath, schema)).stream()
        .map(name -> name.split("\\.")[0]).toList();
  }

  @Override
  public List<String> getSchemas() {
    return FileHelper.listDirectoryNames(catalogPath);
  }
}
