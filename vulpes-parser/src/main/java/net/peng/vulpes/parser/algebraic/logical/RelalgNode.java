package net.peng.vulpes.parser.algebraic.logical;

import lombok.Getter;
import lombok.Setter;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * Relation algebraic node.
 */
@Getter
public abstract class RelalgNode implements RelationAlgebraic {

  /**
   * 这个节点输出数据的结构.
   */
  @Setter
  protected RowHeader rowHeader;

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    if (ObjectUtils.isNotNull(relationAlgebraic)) {
      throw new AstConvertorException(
              String.format("Don't support relalg merge: [%s]. When merging [%s] into [%s]",
                      this.getClass().getName(), this, relationAlgebraic));
    }
    return this;
  }

  /**
   * 用于展示整个关系表达式树.
   */
  public String explain() {
    StringBuilder sb = new StringBuilder();
    return sb.append("-").append(this).toString();
  }

  protected String appendSpaces(int num) {
    StringBuilder sb = new StringBuilder(num);
    for (int i = 0; i < num; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }

  public abstract RelalgNode accept(RelalgNodeVisitor relalgNodeVisitor);
}
