package net.peng.vulpes.common.type;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Description of DataType.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
@ToString
@EqualsAndHashCode
public abstract class DataType {

  public final int precision;

  public final int scale;

  public final boolean nullable;

  protected DataType(int precision, int scale, boolean nullable) {
    this.precision = precision;
    this.scale = scale;
    this.nullable = nullable;
  }

  /**
   * 获取这个类型对应的java类型.
   */
  public abstract Class<?> getJavaType();
}
