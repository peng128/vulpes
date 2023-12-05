package net.peng.vulpes.common.function.aggregate;

import java.util.List;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.function.FunctionName;
import net.peng.vulpes.common.function.MiddleState;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * Description of SumFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
@FunctionName(name = "sum")
@Getter
@ToString
public class SumFunction extends AggregateFunction<Long> {

  private List<Integer> inputColumnIndex;

  private String outputName;

  private MiddleState<Long> state;

  @Deprecated
  public SumFunction(List<Integer> inputColumnIndex, String outputName) {
    this.inputColumnIndex = inputColumnIndex;
    this.outputName = outputName;
  }

  public SumFunction() {
  }

  //TODO
  public Long eval(Integer a) {
    state.merge(Long.getLong(String.valueOf(a)));
    return state.get();
  }

  //TODO
  public Long eval(Long a) {
    state.merge(a);
    return state.get();
  }

  @Override
  public MiddleState<Long> initState() {
    if (ObjectUtils.isNull(state)) {
      state = new SumState();
    }
    return state;
  }

  /**
   * 求和计算的中间状态.
   */
  public static class SumState implements MiddleState<Long> {

    private Long state = 0L;

    @Override
    public Long get() {
      return state;
    }

    @Override
    public void set(Long input) {
      state = input;
    }

    @Override
    public void merge(Long input) {
      state += input;
    }
  }
}
