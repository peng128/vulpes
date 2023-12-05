package net.peng.vulpes.common.function;

/**
 * Description of MiddleState.
 * 用来存储计算中间结果.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
public interface MiddleState<T> {

  /**
   * 获取.
   */
  T get();

  /**
   * 设置.
   */
  void set(T input);

  /**
   * 更新.
   */
  void merge(T input);
}
