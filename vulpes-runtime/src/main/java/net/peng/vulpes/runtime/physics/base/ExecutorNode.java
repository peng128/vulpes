package net.peng.vulpes.runtime.physics.base;

import java.util.List;
import net.peng.vulpes.common.utils.ObjectUtils;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of ExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
public interface ExecutorNode {

  /**
   * 下一个执行节点.
   */
  ExecutorNode next();

  /**
   * 是否有下一个节点.
   */
  default boolean hashNext() {
    return ObjectUtils.isNotNull(next());
  }
}
