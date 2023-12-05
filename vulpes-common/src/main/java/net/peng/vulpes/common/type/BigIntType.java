package net.peng.vulpes.common.type;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.peng.vulpes.common.exception.DataTypeException;

/**
 * Description of BigIntType.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/9
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BigIntType extends DigitalType {
  private static final Integer MAX_PRECISION = 19;

  /**
   * 初始化.
   */
  public BigIntType(int precision, boolean nullable) {
    super(precision, 0, nullable);
    if (precision > MAX_PRECISION) {
      throw new DataTypeException("IntType precision more than 4. Input is: %", precision);
    }
  }

  public BigIntType(boolean nullable) {
    super(4, 0, nullable);
  }

  public BigIntType() {
    super(4, 0, true);
  }

  @Override
  public Class<?> getJavaType() {
    return Long.class;
  }
}
