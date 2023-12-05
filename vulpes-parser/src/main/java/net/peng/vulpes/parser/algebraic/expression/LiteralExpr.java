package net.peng.vulpes.parser.algebraic.expression;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * 字符类型表达式.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class LiteralExpr extends RelalgExpr {

  private final String literal;

  private LiteralExpr(String literal) {
    this.literal = literal;
  }

  @Override
  public String toString() {
    return String.format("%s", literal);
  }

  public static LiteralExpr create(String literal) {
    return new LiteralExpr(literal);
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeaders) {
    return ColumnInfo.builder().name(this.toString()).dataType(new VarcharType()).build();
  }
}
