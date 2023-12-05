package net.peng.vulpes.parser.algebraic.expression;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * 别名表达式.
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
public class AliasExpr extends RelalgExpr {

  /**
   * 表达式.
   */
  private final RelalgExpr relalgExpr;
  /**
   * 别名.
   */
  private final String alias;

  @Override
  public String toString() {
    return String.format("%s as %s", relalgExpr.toString(), alias);
  }

  /**
   * 新建.
   *
   * @param relalgExpr 表达式
   * @param alias      别名
   * @return 新建实体类.
   */
  public static AliasExpr create(final RelalgExpr relalgExpr, final String alias) {
    return new AliasExpr(relalgExpr, alias);
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeader) {
    ColumnInfo columnInfo = relalgExpr.fillColumnInfo(inputHeader);
    return ColumnInfo.builder().name(alias).dataType(columnInfo.getDataType()).build();
  }
}
