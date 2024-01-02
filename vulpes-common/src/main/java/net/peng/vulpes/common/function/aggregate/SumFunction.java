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
  private Double doubleState;

  //TODO
  public Long eval(Integer a) {
    return sumState;
  }

  //TODO
  public Long eval(Long a) {
    return sumState;
  }

  public Double eval(Double a) {
    return null;
  }

  public void init() {
    sumState = 0L;
    doubleState = 0D;
  }

  public void merge(Long a) {
    sumState += a;
  }

  public void merge(Integer a) {
    sumState += a;
  }

  public void merge(Double a) {
    doubleState += a;
  }

  /**
   * 获取结果.
   */
  public Object get() {
    if (doubleState > sumState) {
      return doubleState;
    }
    return sumState;
  }

  public Long getState() {
    return sumState;
  }

}
