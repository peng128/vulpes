package net.peng.vulpes.common.function.aggregate;

import net.peng.vulpes.common.function.Function;
import net.peng.vulpes.common.function.FunctionName;
import net.peng.vulpes.common.function.MiddleState;

/**
 * Description of AggregateFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/3
 */
public abstract class AggregateFunction<S> implements Function {

  public abstract MiddleState<S> initState();
}
