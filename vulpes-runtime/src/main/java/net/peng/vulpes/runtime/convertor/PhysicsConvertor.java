package net.peng.vulpes.runtime.convertor;

import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;

/**
 * Description of inter.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/24
 */
public interface PhysicsConvertor {

  /**
   * 将逻辑执行计划转为物理执行计划.
   */
  ExecutorNode convert(RelalgNode relalgNode, Config config, ExecutorNode nextNode);

  /**
   * 输入是否匹配转换规则.
   */
  boolean isMatch(RelalgNode relalgNode);
}
