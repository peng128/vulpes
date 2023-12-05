package net.peng.vulpes.parser.algebraic.expression;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * 标识符表达式.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class IdentifierExpr extends RelalgExpr {

  private final List<String> identifiers;

  private IdentifierExpr(List<String> identifiers) {
    this.identifiers = identifiers;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String identifier : identifiers) {
      sb.append(identifier).append(".");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  /**
   * 创建.
   *
   * @param str 字符串
   * @return 新对象
   */
  public static IdentifierExpr create(String str) {
    List<String> identifiers = new ArrayList<>();
    identifiers.add(unwrapQuote(str));
    return new IdentifierExpr(identifiers);
  }

  public static IdentifierExpr create(List<String> identifiers) {
    return new IdentifierExpr(identifiers);
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    if (relationAlgebraic instanceof IdentifierExpr) {
      identifiers.addAll(((IdentifierExpr) relationAlgebraic).getIdentifiers());
      return this;
    }
    return super.merge(relationAlgebraic);
  }

  /**
   * 去包裹.
   *
   * @param str 输入字符.
   * @return 字符
   */
  public static String unwrapQuote(String str) {
    if (str.startsWith("\"") && str.endsWith("\"")) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }
}
