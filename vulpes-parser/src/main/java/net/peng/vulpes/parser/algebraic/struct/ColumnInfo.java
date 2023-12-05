package net.peng.vulpes.parser.algebraic.struct;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.peng.vulpes.common.type.DataType;

/**
 * Description of ColumnInfo.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/7
 */
@Data
@Builder
@EqualsAndHashCode
public class ColumnInfo {
  private String name;
  private DataType dataType;
}
