package net.peng.vulpes.parser.algebraic.logical;

import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * Filter action logical relation algebraic node.
 */
@ToString
@Getter
public class RelalgSelection extends SingleInputRelalgNode {

  private final RelalgExpr predicate;


  private RelalgSelection(RelalgExpr predicate) {
    this.predicate = predicate;
  }

  public static RelalgSelection create(RelalgExpr predicate) {
    return new RelalgSelection(predicate);
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    SingleInputRelalgNode singleInputRelalgNode = ObjectUtils.checkClass(relationAlgebraic,
            SingleInputRelalgNode.class, AstConvertorException.class);
    return singleInputRelalgNode.setInput(this);
  }

  @Override
  public RelalgNode accept(RelalgNodeVisitor relalgNodeVisitor) {
    return relalgNodeVisitor.visit(this);
  }

  @Override
  public RowHeader computeOutputHeader(RowHeader inputHeader) {
    predicate.fillColumnInfo(inputHeader);
    return inputHeader;
  }
}
