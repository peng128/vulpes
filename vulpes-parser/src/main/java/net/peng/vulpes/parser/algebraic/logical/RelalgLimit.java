package net.peng.vulpes.parser.algebraic.logical;

import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * Limit关系表达式节点.
 */
@ToString
@Getter
public class RelalgLimit extends SingleInputRelalgNode {

  private final Integer limit;

  private RelalgLimit(Integer limit) {
    this.limit = limit;
  }

  public static RelalgLimit create(Integer limit) {
    return new RelalgLimit(limit);
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    RelalgNode relalgNode = ObjectUtils.checkClass(relationAlgebraic, RelalgNode.class,
            AstConvertorException.class);
    return this.setInput(relalgNode);
  }

  @Override
  public RelalgNode accept(RelalgNodeVisitor relalgNodeVisitor) {
    return relalgNodeVisitor.visit(this);
  }

  @Override
  public RowHeader computeOutputHeader(RowHeader inputHeader) {
    return inputHeader;
  }
}
