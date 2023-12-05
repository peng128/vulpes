package net.peng.vulpes.parser.algebraic.logical;

import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * Description of RelalgAlias.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/12
 */
@Getter
@ToString
public class RelalgAlias extends SingleInputRelalgNode {

  private final String alias;

  private RelalgAlias(String alias) {
    this.alias = alias;
  }

  public static RelalgAlias create(String alias) {
    return new RelalgAlias(alias);
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    if (relationAlgebraic instanceof SingleInputRelalgNode) {
      return ((SingleInputRelalgNode) relationAlgebraic).setInput(this);
    }
    return super.merge(relationAlgebraic);
  }

  @Override
  public RelalgNode accept(RelalgNodeVisitor relalgNodeVisitor) {
    return relalgNodeVisitor.visit(this);
  }

  @Override
  public RowHeader computeOutputHeader(RowHeader inputHeader) {
    return new RowHeader(inputHeader.getColumns(), alias);
  }
}
