package net.peng.vulpes.parser.algebraic.expression;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.type.DateType;
import net.peng.vulpes.common.type.IntervalType;
import net.peng.vulpes.common.type.time.IntervalValue;
import net.peng.vulpes.common.type.time.TimeUnit;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * Description of IntervalExpr.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/29
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class IntervalExpr extends RelalgExpr {

  private final IntervalValue intervalValue;

  public IntervalExpr(IntervalValue intervalValue) {
    this.intervalValue = intervalValue;
  }

  public static IntervalExpr create(Long value, String timeUnit) {
    return new IntervalExpr(new IntervalValue(value, TimeUnit.valueOf(timeUnit)));
  }

  public String toString() {
    return intervalValue.toString();
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeaders) {
    return ColumnInfo.builder().name(this.toString()).dataType(new IntervalType()).build();
  }
}
