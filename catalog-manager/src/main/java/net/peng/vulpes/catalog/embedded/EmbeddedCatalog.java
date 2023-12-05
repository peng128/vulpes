package net.peng.vulpes.catalog.embedded;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.embedded.type.TypeConvertor;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.configuration.FileHelper;
import net.peng.vulpes.common.exception.TableException;
import net.peng.vulpes.common.model.DataFormat;
import net.peng.vulpes.common.model.TableIdentifier;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * Description of EmbeddedCatalog.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
public class EmbeddedCatalog implements Catalog {

  private static final String SCHEMA = "schema";
  private static final String FILE_LIST = "file_list";
  private static final String DATA_FORMAT = "format";

  private final String catalogPath;

  public EmbeddedCatalog(String catalogPath) {
    this.catalogPath = catalogPath;
  }

  @Override
  public TableMeta getTable(TableIdentifier tableIdentifier) {
    if (ObjectUtils.isNull(tableIdentifier.getSchema())) {
      throw new TableException("[%s]Schema 是空，读不出表.", tableIdentifier);
    }
    String filePath = String.format("%s/%s/%s.yaml", catalogPath, tableIdentifier.getSchema(),
            tableIdentifier.getTable());
    TableMetaYamlObject meta = FileHelper.yamlReader(filePath, TableMetaYamlObject.class);
    List<String> fieldNames = new ArrayList<>(meta.schema().size());
    List<DataType> dataTypes = new ArrayList<>(meta.schema().size());
    meta.schema().forEach((key, value) -> {
      fieldNames.add(key);
      dataTypes.add(TypeConvertor.convert(value));
    });
    return new EmbeddedTableMeta(fieldNames, dataTypes, meta.files(), meta.format);
  }

  private record TableMetaYamlObject(LinkedHashMap<String, String> schema, List<String> files,
                                     DataFormat format) {
  }
}
