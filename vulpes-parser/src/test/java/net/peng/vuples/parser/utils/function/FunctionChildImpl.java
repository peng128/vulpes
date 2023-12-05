package net.peng.vuples.parser.utils.function;

import net.peng.vulpes.common.function.FunctionName;
import net.peng.vulpes.common.function.scalar.ScalarFunction;

/**
 * Description of FunctionImpl.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
@FunctionName(name = "test_function_child")
public class FunctionChildImpl extends ScalarFunction {

  public Integer eval(Object a) {
    return 1;
  }
}
