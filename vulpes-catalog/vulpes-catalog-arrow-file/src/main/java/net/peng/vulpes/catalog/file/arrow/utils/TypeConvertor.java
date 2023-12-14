package net.peng.vulpes.catalog.file.arrow.utils;

import net.peng.vulpes.common.exception.DataTypeException;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.common.type.VarcharType;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.FieldType;

/**
 * Description of TypeConvertor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/11
 */
public class TypeConvertor {
  public static final String VARCHAR = "VARCHAR";
  public static final String INT = "INT";
  public static final String BIGINT = "BIGINT";

  /**
   * 从平文本转换为字段类型.
   */
  public static DataType convert(FieldType input) {
    //TODO: 这里要补充精度以及是否为空的判断.
    if (input.getType() instanceof ArrowType.Utf8) {
      return new VarcharType();
    } else if (input.getType() instanceof ArrowType.Int) {
      return new IntType();
    } else if (input.getType() instanceof ArrowType.Decimal) {
      return new BigIntType();
    } else if (input.getType() instanceof ArrowType.FloatingPoint) {
      // TODO: 新创建这个类型
      return new BigIntType();
    } else if (input.getType() instanceof ArrowType.Date) {
      // TODO: 新创建这个类型
      return new VarcharType();
    }
    throw new DataTypeException("无法找到对应类型 %s.", input.getType().getTypeID());
  }
}
