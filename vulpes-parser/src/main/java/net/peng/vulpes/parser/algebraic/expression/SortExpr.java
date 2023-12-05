package net.peng.vulpes.parser.algebraic.expression;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * 排序关系表达式节点.
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = true)
public class SortExpr extends RelalgExpr {
  private final RelalgExpr expr;
  private final SortKind sortKind;

  private SortExpr(RelalgExpr expr, SortKind sortKind) {
    this.expr = expr;
    this.sortKind = sortKind;
  }

  public static SortExpr create(RelalgExpr expr, SortKind sortKind) {
    return new SortExpr(expr, sortKind);
  }

  public static SortExpr create(RelalgExpr expr) {
    return new SortExpr(expr, SortKind.ASC);
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeader) {
    return expr.fillColumnInfo(inputHeader);
  }

  /**
   * 排序顺序.
   */
  public enum SortKind {
    ASC, DESC;
  }
}
