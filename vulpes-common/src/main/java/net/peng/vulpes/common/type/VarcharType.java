package net.peng.vulpes.common.type;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Description of VarCharType.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VarcharType extends DataType {

  public VarcharType(int precision, boolean nullable) {
    super(precision, 0, nullable);
  }

  public VarcharType(boolean nullable) {
    super(Integer.MAX_VALUE, 0, nullable);
  }

  public VarcharType() {
    super(Integer.MAX_VALUE, 0, true);
  }

  @Override
  public Class<?> getJavaType() {
    return String.class;
  }
}
