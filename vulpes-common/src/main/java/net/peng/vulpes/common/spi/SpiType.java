package net.peng.vulpes.common.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description of SpiType.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpiType {
  /**
   * 用来标识子类类型的.
   */
  String value();
}
