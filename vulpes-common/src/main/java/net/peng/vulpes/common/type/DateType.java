package net.peng.vulpes.common.type;

import java.time.LocalDate;

/**
 * Description of DateType.
 * 日期类型 - 2023-12-29.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/29
 */
public class DateType extends DataType {

  public DateType(boolean nullable) {
    super(0, 0, nullable);
  }

  public DateType() {
    super(0, 0, false);
  }

  @Override
  public Class<?> getJavaType() {
    return LocalDate.class;
  }
}
