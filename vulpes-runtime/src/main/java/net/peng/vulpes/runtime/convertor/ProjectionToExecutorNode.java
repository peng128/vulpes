package net.peng.vulpes.runtime.convertor;

import java.util.ArrayList;
import java.util.List;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgProjection;
import net.peng.vulpes.parser.utils.FunctionUtils;
import net.peng.vulpes.runtime.physics.AggregateExecutorNode;
import net.peng.vulpes.runtime.physics.ProjectionExecutorNode;
import net.peng.vulpes.runtime.physics.ProjectionOnlyExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import org.apache.commons.lang3.tuple.Pair;

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
    final Pair<Boolean, List<RelalgExpr>> aggFunctionPair =
        FunctionUtils.allAggFunctions(relalgProjection.getProjects());
    if (aggFunctionPair.getLeft()) {
      return new AggregateExecutorNode(nextNode, List.of(), relalgProjection.getProjects(),
          relalgProjection.getInput().getRowHeader(), relalgProjection.getRowHeader());
    }
    if (aggFunctionPair.getRight().size() != relalgProjection.getProjects().size()) {
      throw new ComputeException("这些字段没有在group by 内. %s", aggFunctionPair.getRight());
    }
    return new ProjectionExecutorNode(nextNode, relalgProjection.getProjects(),
        relalgProjection.getInput().getRowHeader(), relalgProjection.getRowHeader());
  }

  @Override
  public boolean isMatch(RelalgNode relalgNode) {
    return relalgNode instanceof RelalgProjection;
  }
}
