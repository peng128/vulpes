package net.peng.vulpes.parser.utils;

import java.util.Optional;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.AliasExpr;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.NumericExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.logical.RelalgAlias;

/**
 * 对应表达式的工具类.
 *
 * @author peng
 * @version 1.0
 * @since 2023-09-12
 */
public class ExpressionUtils {

  /**
   * 判断这个表达式是否是函数.
   */
  public static boolean isFunction(final RelalgExpr relalgExpr) {
    if (relalgExpr instanceof FunctionRef) {
      return true;
    }
    if (relalgExpr instanceof AliasExpr) {
      return isFunction(((AliasExpr) relalgExpr).getRelalgExpr());
    }
    return false;
  }

  /**
   * 如果 {@link RelalgExpr}保存的信息是字段索引信息，则直接提取.
   *
   * @param relalgExpr 表达式
   * @return 索引
   */
  public static Optional<Integer> unwrapIndexIf(RelalgExpr relalgExpr) {
    if (relalgExpr instanceof NumericExpr) {
      Object index = ((NumericExpr) relalgExpr).getNumeric();
      if (!(index instanceof Integer)) {
        throw new AstConvertorException("%s can't apply to column index.", index);
      }
      return Optional.of((Integer) index);
    }
    return Optional.empty();
  }

  /**
   * 判断两个表达式是否相同.
   */
  public static boolean equals(RelalgExpr left, RelalgExpr right) {
    if (left.equals(right)) {
      return true;
    }
    return equalsByAlias(left, right) || equalsByAlias(right, left);
  }

  /**
   * 判断别名表达式是否指向传入表达式.
   */
  public static boolean equalsByAlias(RelalgExpr alias, RelalgExpr expr) {
    if (!(alias instanceof AliasExpr)) {
      return false;
    }
    String name = ((AliasExpr) alias).getAlias();
    if (expr instanceof AliasExpr) {
      if (((AliasExpr) expr).getAlias().equals(name)) {
        return true;
      }
    }
    if (expr instanceof ColumnNameExpr) {
      if (((ColumnNameExpr) expr).getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 从代数表达式中提取别名.若存在的话.
   */
  public static Optional<String> getAliasNameIfExist(RelationAlgebraic relationAlgebraic) {
    if (relationAlgebraic instanceof RelalgAlias) {
      RelalgAlias relalgAlias = (RelalgAlias) relationAlgebraic;
      if (!ObjectUtils.isEmpty(relalgAlias.getAlias())) {
        return Optional.of(relalgAlias.getAlias());
      }
    }
    if (relationAlgebraic instanceof AliasExpr) {
      AliasExpr aliasExpr = (AliasExpr) relationAlgebraic;
      return Optional.of(aliasExpr.getAlias());
    }
    return Optional.empty();
  }
}
