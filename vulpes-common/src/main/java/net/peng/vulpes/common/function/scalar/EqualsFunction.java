package net.peng.vulpes.common.function.scalar;

import net.peng.vulpes.common.function.FunctionName;

/**
 * Description of EqualsFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
@FunctionName(name = "=", kind = 1)
public class EqualsFunction extends ScalarFunction {

  /**
   * 执行逻辑.
   */
  public Boolean eval(Object a, Object b) {
    if (a.equals(b)) {
      return true;
    }
    return a.toString().equals(b.toString());
  }
}
