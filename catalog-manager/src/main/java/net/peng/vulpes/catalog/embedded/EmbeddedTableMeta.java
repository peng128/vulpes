package net.peng.vulpes.catalog.embedded;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.catalog.table.FileTableMeta;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.model.DataFormat;
import net.peng.vulpes.common.type.DataType;

/**
 * Description of EmbeddedTableMeta.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EmbeddedTableMeta extends FileTableMeta {

  /**
   * 内置表元信息.
   */
  public EmbeddedTableMeta(List<String> fieldNames, List<DataType> fieldTypes,
                           List<String> dataFiles, DataFormat dataFormat) {
    super(fieldNames, fieldTypes, dataFiles, dataFormat);
  }
}
