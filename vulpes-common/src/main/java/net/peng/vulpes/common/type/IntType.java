package net.peng.vulpes.common.type;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.peng.vulpes.common.exception.DataTypeException;

/**
 * Description of IntType.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IntType extends DigitalType {
  private static final Integer MAX_PRECISION = 10;

  /**
   * 初始化.
   */
  public IntType(int precision, boolean nullable) {
    super(precision, 0, nullable);
    if (precision > MAX_PRECISION) {
      throw new DataTypeException("IntType precision more than 4. Input is: %", precision);
    }
  }

  public IntType(boolean nullable) {
    super(4, 0, nullable);
  }

  public IntType() {
    super(4, 0, true);
  }

  @Override
  public Class<?> getJavaType() {
    return Integer.class;
  }
}
