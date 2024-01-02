package net.peng.vulpes.common.function.aggregate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.function.FunctionName;

/**
 * Description of AvgFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2024/1/2
 */
@FunctionName(name = "avg")
public class AvgFunction extends AggregateFunction {
  private AvgState avgState;

  //TODO
  public Double eval(Integer a) {
    avgState.countState++;
    return avgState.sumState;
  }

  //TODO
  public Double eval(Long a) {
    avgState.countState++;
    return avgState.sumState;
  }

  public Double eval(Double a) {
    avgState.countState++;
    return avgState.sumState;
  }

  public void init() {
    avgState = new AvgState(0D, 0L);
  }

  public void merge(Long a) {
    avgState.countState++;
    avgState.sumState += a;
  }

  public void merge(Integer a) {
    avgState.countState++;
    avgState.sumState += a;
  }

  public void merge(Double a) {
    avgState.countState++;
    avgState.sumState += a;
  }

  public Double get() {
    return avgState.sumState / avgState.countState;
  }

  public AvgState getState() {
    return avgState;
  }

  /**
   * 平均值中间状态.
   */
  @ToString
  @Getter
  @AllArgsConstructor
  public static class AvgState {
    private Double sumState;
    private Long countState;
  }
}
