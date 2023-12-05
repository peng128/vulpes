package net.peng.vuples.parser.visitor;

import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgScan;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * Description of SimpleRelalgNodeVisitor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class SimpleRelalgNodeVisitor extends RelalgNodeVisitor {
  public int counter = 0;

  @Override
  public RelalgNode visit(RelalgScan relalgScan) {
    counter++;
    return super.visit(relalgScan);
  }
}
