package net.peng.vuples.parser.utils.function;

import net.peng.vulpes.common.function.FunctionName;
import net.peng.vulpes.common.function.scalar.ScalarFunction;

/**
 * Description of FunctionMultiInput.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
@FunctionName(name = "test_function_multi")
public class FunctionMultiInput extends ScalarFunction {

  public Long eval(Integer a, Long b) {
    return a + b;
  }
}
