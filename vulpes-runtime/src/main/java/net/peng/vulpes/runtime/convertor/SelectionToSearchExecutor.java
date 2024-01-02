package net.peng.vulpes.runtime.convertor;

import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgSelection;
import net.peng.vulpes.runtime.physics.SelectionExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;

/**
 * Description of FilterToSearchExecutor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/25
 */
public class SelectionToSearchExecutor implements PhysicsConvertor {

  public static final PhysicsConvertor CONVERTOR = new SelectionToSearchExecutor();

  @Override
  public ExecutorNode convert(RelalgNode relalgNode, Config config, ExecutorNode nextNode) {
    RelalgSelection relalgSelection = (RelalgSelection) relalgNode;
    return new SelectionExecutorNode(nextNode, relalgSelection.getPredicate(),
        relalgSelection.getInput().getRowHeader(), relalgSelection.getRowHeader());
  }

  @Override
  public boolean isMatch(RelalgNode relalgNode) {
    return relalgNode instanceof RelalgSelection;
  }
}
