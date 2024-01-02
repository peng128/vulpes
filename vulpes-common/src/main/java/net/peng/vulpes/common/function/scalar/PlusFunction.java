package net.peng.vulpes.common.function.scalar;

import java.time.LocalDate;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.function.FunctionName;
import net.peng.vulpes.common.type.time.IntervalValue;

/**
 * Description of PlusFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
@FunctionName(name = "+", kind = 1)
public class PlusFunction extends ScalarFunction {

  public Integer eval(Integer a, Integer b) {
    return a + b;
  }

  public Long eval(Long a, Long b) {
    return a + b;
  }

  public Long eval(Integer a, Long b) {
    return a + b;
  }

  public Long eval(Long a, Integer b) {
    return a + b;
  }

  public Double eval(Integer a, Double b) {
    return a + b;
  }

  /**
   * 用于时间相加.
   */
  public LocalDate eval(LocalDate a, IntervalValue b) {
    switch (b.getTimeUnit()) {
      case DAY:
        return a.plusDays(b.getValue());
      default:
        throw new ComputeException("不支持日期格式 %s", b.getTimeUnit());
    }
  }
}
