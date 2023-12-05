package net.peng.vulpes.common.model.context;

/**
 * 用来对上下文进行包裹.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/16
 */
public interface Context {

  /**
   * 获取上下文中，选定的类型.
   */
  <T> T get(Class<T> clazz);
}
