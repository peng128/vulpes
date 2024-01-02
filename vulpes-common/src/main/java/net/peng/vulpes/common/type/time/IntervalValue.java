package net.peng.vulpes.common.type.time;

import lombok.Getter;
import net.peng.vulpes.common.exception.ComputeException;

/**
 * Description of IntervalValue.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/29
 */
@Getter
public class IntervalValue {

  private final Long value;

  private final TimeUnit timeUnit;

  public IntervalValue(Long value, TimeUnit timeUnit) {
    this.value = value;
    this.timeUnit = timeUnit;
  }

  /**
   * 转为毫秒.
   */
  public Long toEpochMillisecond() {
    return switch (timeUnit) {
      case DAY -> value * 24 * 60 * 60 * 1000;
      case HOUR -> value * 60 * 60 * 1000;
      case MINUTE -> value * 60 * 1000;
      case SECOND -> value * 1000;
      case MILLISECOND -> value;
      default -> throw new ComputeException("不支持 %s 类型的获取.", timeUnit);
    };
  }

  /**
   * 从毫秒生成{@link IntervalValue}.
   */
  public static IntervalValue fromEpochMillisecond(Long millisecond) {
    if (millisecond > 24 * 60 * 60 * 1000) {
      return new IntervalValue(millisecond / (24 * 60 * 60 * 1000), TimeUnit.DAY);
    } else if (millisecond > 60 * 60 * 1000) {
      return new IntervalValue(millisecond / (60 * 60 * 1000), TimeUnit.HOUR);
    } else if (millisecond > 60 * 1000) {
      return new IntervalValue(millisecond / (60 * 1000), TimeUnit.MINUTE);
    } else if (millisecond > 1000) {
      return new IntervalValue(millisecond / 1000, TimeUnit.SECOND);
    }
    return new IntervalValue(millisecond, TimeUnit.MILLISECOND);
  }

  public String toString() {
    return String.format("INTERVAL '%s' %s", value, timeUnit);
  }
}
