package net.peng.vulpes.common.type;

import net.peng.vulpes.common.type.time.IntervalValue;

/**
 * Description of IntervalType.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/29
 */
public class IntervalType extends DataType {

  public IntervalType() {
    super(0, 0, false);
  }

  @Override
  public Class<?> getJavaType() {
    return IntervalValue.class;
  }
}
