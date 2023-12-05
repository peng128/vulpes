package net.peng.vulpes.parser.visitor;

import java.util.List;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.logical.InputRelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgAggregation;
import net.peng.vulpes.parser.algebraic.logical.RelalgAlias;
import net.peng.vulpes.parser.algebraic.logical.RelalgJoin;
import net.peng.vulpes.parser.algebraic.logical.RelalgLimit;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgProjection;
import net.peng.vulpes.parser.algebraic.logical.RelalgScan;
import net.peng.vulpes.parser.algebraic.logical.RelalgSelection;
import net.peng.vulpes.parser.algebraic.logical.RelalgSort;
import net.peng.vulpes.parser.algebraic.logical.RelalgUnion;

/**
 * Description of RelNodeVisitor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public abstract class RelalgNodeVisitor {

  protected RelalgNode visitChildren(RelalgNode relalgNode) {
    if (!(relalgNode instanceof InputRelalgNode)) {
      return relalgNode;
    }
    final List<RelalgNode> inputs = ((InputRelalgNode) relalgNode).getInputs();
    if (ObjectUtils.isNull(inputs)) {
      return relalgNode;
    }
    for (RelalgNode input : inputs) {
      input.accept(this);
    }
    return relalgNode;
  }

  public RelalgNode visit(RelalgAggregation relalgAggregation) {
    return visitChildren(relalgAggregation);
  }

  public RelalgNode visit(RelalgAlias relalgAlias) {
    return visitChildren(relalgAlias);
  }

  public RelalgNode visit(RelalgJoin relalgJoin) {
    return visitChildren(relalgJoin);
  }

  public RelalgNode visit(RelalgLimit relalgLimit) {
    return visitChildren(relalgLimit);
  }

  public RelalgNode visit(RelalgProjection relalgProjection) {
    return visitChildren(relalgProjection);
  }

  public RelalgNode visit(RelalgScan relalgScan) {
    return relalgScan;
  }

  public RelalgNode visit(RelalgSelection relalgSelection) {
    return visitChildren(relalgSelection);
  }

  public RelalgNode visit(RelalgSort relalgSort) {
    return visitChildren(relalgSort);
  }

  public RelalgNode visit(RelalgUnion relalgUnion) {
    return visitChildren(relalgUnion);
  }

  public RelalgNode visitOthers(RelalgNode relalgNode) {
    return visitChildren(relalgNode);
  }
}
