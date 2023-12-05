package net.peng.vulpes.parser.algebraic.logical;

import java.util.List;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.SortExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * 排序关系表达式.
 */
@ToString
@Getter
public class RelalgSort extends SingleInputRelalgNode {

  private final List<SortExpr> items;

  private RelalgSort(List<SortExpr> items) {
    this.items = items;
  }

  public static RelalgSort create(List<SortExpr> items) {
    return new RelalgSort(items);
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    if (ObjectUtils.isNotNull(this.getInputs())) {
      return relationAlgebraic.merge(this);
    }
    return this.setInput(ObjectUtils.checkClass(relationAlgebraic, RelalgNode.class,
            AstConvertorException.class));
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
