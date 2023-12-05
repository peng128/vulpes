package net.peng.vulpes.parser.algebraic.expression;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * 数字类型表达式.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class NumericExpr extends RelalgExpr {
  private final String text;
  private final Object numeric;

  private NumericExpr(String text) {
    this.text = text;
    this.numeric = convertFromNumeric(text);
  }

  @Override
  public String toString() {
    return text;
  }

  public static NumericExpr create(String text) {
    return new NumericExpr(text);
  }

  private static Object convertFromNumeric(String str) {
    try {
      // 尝试将字符串解析为整数
      return Integer.parseInt(str);
    } catch (NumberFormatException e1) {
      try {
        // 尝试将字符串解析为长整数
        return Long.parseLong(str);
      } catch (NumberFormatException e2) {
        try {
          // 尝试将字符串解析为浮点数
          return Double.parseDouble(str);
        } catch (NumberFormatException e3) {
          // 如果无法解析为数字，则返回null或抛出异常，根据需求而定
          return null;
        }
      }
    }
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeaders) {
    ColumnInfo.ColumnInfoBuilder columnInfoBuilder = ColumnInfo.builder().name(this.toString());
    if (numeric instanceof Integer) {
      columnInfoBuilder.dataType(new IntType(false));
    } else if (numeric instanceof Long) {
      columnInfoBuilder.dataType(new BigIntType(false));
    } else {
      throw new AstConvertorException("不能推断数字类型:[] []", numeric.toString(),
              numeric.getClass().getName());
    }
    return columnInfoBuilder.build();
  }
}
