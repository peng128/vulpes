package net.peng.vulpes.runtime.function.execute;

import net.peng.vulpes.runtime.memory.MemorySpace;

/**
 * Description of ExpressionExecutor.
 * 表达式执行器.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/28
 */
public interface ExpressionExecutor<T, R> {

  /**
   * 执行表达式中的逻辑.
   *
   * @param data 输入的所有列数据(不止包含这个函数使用到的列).
   * @param memorySpace     内存空间.
   * @return 一列的数据
   */
  R execute(T data, MemorySpace memorySpace);
}
