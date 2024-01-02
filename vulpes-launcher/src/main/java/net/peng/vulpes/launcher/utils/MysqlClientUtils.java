package net.peng.vulpes.launcher.utils;

import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.BooleanType;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.type.DateType;
import net.peng.vulpes.common.type.DoubleType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vuples.jdbc.mysql.mycat.Fields;

/**
 * Description of MysqlClientUtils.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
public class MysqlClientUtils {

  /**
   * 将数据类型映射为Mysql客户端的数据类型.
   */
  public static Integer dataTypeConvert(DataType dataType) {
    if (dataType instanceof VarcharType) {
      return Fields.FIELD_TYPE_VAR_STRING;
    } else if (dataType instanceof IntType) {
      return Fields.FIELD_TYPE_INT24;
    } else if (dataType instanceof BigIntType) {
      return Fields.FIELD_TYPE_LONG;
    } else if (dataType instanceof BooleanType) {
      return Fields.FIELD_TYPE_BIT;
    } else if (dataType instanceof DoubleType) {
      return Fields.FIELD_TYPE_DOUBLE;
    } else if (dataType instanceof DateType) {
      return Fields.FIELD_TYPE_DATE;
    }
    throw new ComputeException("不认识的数据类型 %s，无法发送给客户端.", dataType.getClass().getName());
  }
}
