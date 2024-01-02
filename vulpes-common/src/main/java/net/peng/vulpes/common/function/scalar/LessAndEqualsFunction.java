package net.peng.vulpes.common.function.scalar;

import java.time.LocalDate;
import net.peng.vulpes.common.function.FunctionName;

/**
 * Description of PlusFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
@FunctionName(name = "<=", kind = 1)
public class LessAndEqualsFunction extends ScalarFunction {

  public Boolean eval(Integer a, Integer b) {
    return a <= b;
  }

  public Boolean eval(Long a, Long b) {
    return a <= b;
  }

  public Boolean eval(Integer a, Long b) {
    return a <= b;
  }

  public Boolean eval(Long a, Integer b) {
    return a <= b;
  }

  public Boolean eval(LocalDate a, LocalDate b) {
    return !a.isAfter(b);
  }
}
