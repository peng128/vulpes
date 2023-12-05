package net.peng.vulpes.catalog.table;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.model.DataFormat;
import net.peng.vulpes.common.type.DataType;

/**
 * Description of FileTableMeta.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/24
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FileTableMeta extends TableMeta {
  private final List<String> dataFiles;

  private final DataFormat dataFormat;

  protected FileTableMeta(List<String> fieldNames, List<DataType> fieldTypes,
                          List<String> dataFiles, DataFormat dataFormat) {
    super(fieldNames, fieldTypes);
    this.dataFiles = dataFiles;
    this.dataFormat = dataFormat;
  }
}
