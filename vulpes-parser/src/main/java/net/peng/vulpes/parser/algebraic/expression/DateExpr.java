package net.peng.vulpes.parser.algebraic.expression;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.common.type.DateType;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * Description of DateExpr.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/29
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class DateExpr extends RelalgExpr {

  private final LocalDate date;

  private DateExpr(LocalDate date) {
    this.date = date;
  }

  public static DateExpr create(LocalDate date) {
    return new DateExpr(date);
  }

  public static DateExpr create(String date) {
    return new DateExpr(LocalDate.parse(date));
  }

  public static DateExpr create(LiteralExpr date) {
    return new DateExpr(LocalDate.parse(date.getLiteral()));
  }

  public String toString() {
    return date.toString();
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeaders) {
    return ColumnInfo.builder().name(this.toString()).dataType(new DateType()).build();
  }
}
