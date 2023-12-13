package net.peng.vulpes.common.function.aggregate;

import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.function.FunctionName;

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
public class SumFunction extends AggregateFunction {

  private Long sumState;

  //TODO
  public Long eval(Integer a) {
    return sumState;
  }

  //TODO
  public Long eval(Long a) {
    return sumState;
  }

  public void init() {
    sumState = 0L;
  }

  public void merge(Long a) {
    sumState += a;
  }

  public void merge(Integer a) {
    sumState += a;
  }

  public Long get() {
    return sumState;
  }

  public Long getState() {
    return sumState;
  }

}
