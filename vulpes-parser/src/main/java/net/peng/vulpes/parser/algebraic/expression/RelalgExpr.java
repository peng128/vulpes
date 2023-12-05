package net.peng.vulpes.parser.algebraic.expression;

import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * 关系表达式.
 */
public abstract class RelalgExpr implements RelationAlgebraic {

  /**
   * 填充字段中的索引，并返回字段信息.
   */
  public ColumnInfo fillColumnInfo(RowHeader inputHeaders) {
    return ColumnInfo.builder().name(this.toString()).build();
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    if (ObjectUtils.isNotNull(relationAlgebraic)) {
      throw new AstConvertorException(
              String.format("Don't support RelalgExpr merge [%s], input: [%s].", this,
                      relationAlgebraic));
    }
    return this;
  }
}
