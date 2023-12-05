package net.peng.vulpes.parser.algebraic.logical;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.ExpressionUtils;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * union 关系表达式.
 */
@ToString
@Getter
public class RelalgUnion extends InputRelalgNode {
  private final boolean all;
  private final Type type;

  private RelalgUnion(boolean all, Type type, List<RelalgNode> inputs) {
    this.all = all;
    this.type = type;
    this.inputs = inputs;
    if (inputs.stream().anyMatch(relalgNode -> ObjectUtils.isNotNull(relalgNode.rowHeader))) {
      this.rowHeader = new RowHeader(inputs.get(0).rowHeader,
              ExpressionUtils.getAliasNameIfExist(inputs.get(0)));
    }
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

  public static RelalgUnion create(boolean all, Type type, List<RelalgNode> inputs) {
    return new RelalgUnion(all, type, inputs);
  }

  /**
   * union类型.
   */
  public enum Type {
    UNION, EXCEPT, INTERSECT;
  }
}
