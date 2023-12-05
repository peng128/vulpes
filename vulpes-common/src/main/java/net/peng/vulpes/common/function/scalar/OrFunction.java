package net.peng.vulpes.common.function.scalar;

import net.peng.vulpes.common.function.FunctionName;

/**
 * Description of PlusFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
@FunctionName(name = "OR", kind = 1)
public class OrFunction extends ScalarFunction {

  public Boolean eval(Boolean a, Boolean b) {
    return a || b;
  }
}
