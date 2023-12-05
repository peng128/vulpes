package net.peng.vulpes.common.function.aggregate;

import net.peng.vulpes.common.function.FunctionName;
import net.peng.vulpes.common.function.MiddleState;

/**
 * Description of CountFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
@FunctionName(name = "count")
public class CountFunction extends AggregateFunction {

  @Override
  public MiddleState initState() {
    return null;
  }
}
