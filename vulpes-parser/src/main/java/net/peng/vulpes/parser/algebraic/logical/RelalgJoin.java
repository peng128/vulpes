package net.peng.vulpes.parser.algebraic.logical;

import java.util.Arrays;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.ExpressionUtils;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * 关联join关系表达式节点.
 */
@ToString
@Getter
public class RelalgJoin extends InputRelalgNode {

  private final JoinType joinType;
  private final JoinSide joinSide;
  private final RelalgExpr condition;

  private RelalgJoin(JoinType joinType, JoinSide joinSide, RelalgNode left,
                     RelalgNode right, RelalgExpr condition) {
    this.joinType = joinType;
    this.joinSide = joinSide;
    // 这里默认使用从左到右的顺序.
    this.inputs = Arrays.asList(left, right);
    this.condition = condition;
    if (ObjectUtils.isNotNull(left.rowHeader, right.rowHeader)) {
      this.rowHeader = new RowHeader(left.rowHeader,
              ExpressionUtils.getAliasNameIfExist(left));
      rowHeader.addRowHeader(right.rowHeader,
              ExpressionUtils.getAliasNameIfExist(right).orElse(""));
    }
  }

  public RelalgNode getLeft() {
    return getInputs().get(0);
  }

  public RelalgNode getRight() {
    return getInputs().get(1);
  }

  public static RelalgJoin create(JoinType joinType, JoinSide joinSide, RelalgNode left,
                                  RelalgNode right, RelalgExpr condition) {
    return new RelalgJoin(joinType, joinSide, left, right, condition);
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

  /**
   * JOIN种类.
   */
  public enum JoinType {
    INNER_JOIN, OUTER_JOIN, CROSS_JOIN;
  }

  /**
   * JOIN方式.
   */
  public enum JoinSide {
    LEFT, RIGHT, FULL;
  }
}
