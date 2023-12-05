package net.peng.vulpes.catalog.table;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.type.DataType;

/**
 * 表的元数据.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
@ToString
@EqualsAndHashCode
@Getter
public abstract class TableMeta {

  protected final List<String> fieldNames;
  protected final List<DataType> fieldTypes;

  protected TableMeta(List<String> fieldNames, List<DataType> fieldTypes) {
    this.fieldNames = fieldNames;
    this.fieldTypes = fieldTypes;
  }
}
