package net.peng.vulpes.common.function.aggregate;

import net.peng.vulpes.common.function.FunctionName;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * Description of CountFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
@FunctionName(name = "count")
public class CountFunction extends AggregateFunction {
  private Long countState;

  //TODO
  public Long eval(Object a) {
    return countState;
  }

  //TODO
  public Long eval() {
    return countState;
  }

  /**
   * count.
   */
  public void init() {
    countState = 0L;
  }

  /**
   * count.
   */
  public void merge(Object a) {
    if (ObjectUtils.isNotNull(a)) {
      countState++;
    }
  }

  /**
   * count.
   */
  public void merge() {
    countState++;
  }

  /**
   * count.
   */
  public Long get() {
    return countState;
  }

  /**
   * count.
   */
  public Long getState() {
    return countState;
  }
}
