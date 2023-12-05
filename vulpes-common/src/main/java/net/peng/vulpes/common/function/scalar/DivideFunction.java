package net.peng.vulpes.common.function.scalar;

import net.peng.vulpes.common.function.FunctionName;

/**
 * Description of PlusFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
@FunctionName(name = "*", kind = 1)
public class DivideFunction extends ScalarFunction {

  public Integer eval(Integer a, Integer b) {
    return a / b;
  }

  public Long eval(Long a, Long b) {
    return a / b;
  }

  public Long eval(Integer a, Long b) {
    return a / b;
  }

  public Long eval(Long a, Integer b) {
    return a / b;
  }
}
