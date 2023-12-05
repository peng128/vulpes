package net.peng.vulpes.common.type;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Description of BooleanType.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/9
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BooleanType extends DataType {

  protected BooleanType(boolean nullable) {
    super(1, 0, nullable);
  }

  public BooleanType() {
    this(false);
  }

  @Override
  public Class<?> getJavaType() {
    return Boolean.class;
  }
}
