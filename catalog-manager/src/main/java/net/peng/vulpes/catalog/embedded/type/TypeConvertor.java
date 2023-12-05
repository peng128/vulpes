package net.peng.vulpes.catalog.embedded.type;

import net.peng.vulpes.common.exception.DataTypeException;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.common.type.VarcharType;

/**
 * Description of Convertor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
public class TypeConvertor {
  public static final String VARCHAR = "VARCHAR";
  public static final String INT = "INT";
  public static final String BIGINT = "BIGINT";

  /**
   * 从平文本转换为字段类型.
   */
  public static DataType convert(String input) {
    //TODO: 这里要补充精度以及是否为空的判断.
    if (input.toUpperCase().startsWith(VARCHAR)) {
      return new VarcharType();
    } else if (input.toUpperCase().startsWith(INT)) {
      return new IntType();
    } else if (input.toUpperCase().startsWith(BIGINT)) {
      return new BigIntType();
    }
    throw new DataTypeException("无法找到对应类型 %s.", input);
  }
}
