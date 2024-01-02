package net.peng.vulpes.common.type;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.peng.vulpes.common.exception.DataTypeException;

/**
 * Description of DoubleType.
 *
 * @author peng
 * @version 1.0
 * @since 2024/1/2
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DoubleType extends DigitalType {
  private static final Integer MAX_PRECISION = 19;

  /**
   * 初始化.
   */
  public DoubleType(int precision, boolean nullable) {
    super(precision, 0, nullable);
    if (precision > MAX_PRECISION) {
      throw new DataTypeException("IntType precision more than 4. Input is: %", precision);
    }
  }

  public DoubleType(boolean nullable) {
    super(19, 10, nullable);
  }

  public DoubleType() {
    super(19, 10, true);
  }

  @Override
  public Class<?> getJavaType() {
    return Double.class;
  }
}
