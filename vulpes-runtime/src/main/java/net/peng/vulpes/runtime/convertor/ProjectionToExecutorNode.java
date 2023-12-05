package net.peng.vulpes.runtime.convertor;

import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgProjection;
import net.peng.vulpes.runtime.physics.ProjectionExecutorNode;
import net.peng.vulpes.runtime.physics.ProjectionOnlyExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;

/**
 * Description of ProjectionToExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class ProjectionToExecutorNode implements PhysicsConvertor {

  public static final PhysicsConvertor CONVERTOR = new ProjectionToExecutorNode();

  @Override
  public ExecutorNode convert(RelalgNode relalgNode, Config config, ExecutorNode nextNode) {
    RelalgProjection relalgProjection = (RelalgProjection) relalgNode;
    if (ObjectUtils.isEmpty(relalgProjection.getInputs())) {
      // 兼容select a + b 这种没有table scan的语句.
      return new ProjectionOnlyExecutorNode(nextNode, relalgProjection.getProjects(),
              null, relalgProjection.getRowHeader());
    }
    return new ProjectionExecutorNode(nextNode, relalgProjection.getProjects(),
            relalgProjection.getInput().getRowHeader(), relalgProjection.getRowHeader());
  }

  @Override
  public boolean isMatch(RelalgNode relalgNode) {
    return relalgNode instanceof RelalgProjection;
  }
}
