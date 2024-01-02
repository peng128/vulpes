package net.peng.vulpes.parser.utils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.BooleanType;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.type.DateType;
import net.peng.vulpes.common.type.DoubleType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.function.OperatorSymbol;
import net.peng.vulpes.parser.algebraic.function.TypeSpeculate;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * Description of DataTypeEstimateUtils.
 * 用来预估数据类型.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/9
 */
public class DataTypeEstimateUtils {

  /**
   * 根据输入和方法操作服，推断输出类型.
   */
  @Deprecated
  public static DataType functionResultSpeculate(String operator, List<DataType> inputDataTypes) {
    final List<OperatorSymbol> operatorSymbols = Arrays.asList(OperatorSymbol.values());
    for (OperatorSymbol operatorSymbol : operatorSymbols) {
      if (operatorSymbol.value.equalsIgnoreCase(operator)) {
        TypeSpeculate typeSpeculate = operatorSymbol.typeSpeculate.build(inputDataTypes);
        return typeSpeculate.getDataType();
      }
    }
    //TODO 先默认返回一个字符串.
    return new VarcharType();
    //throw new ComputeException("找不到方法[%s] 可使用列表为 [%s]", operator, operatorSymbols);
  }

  /**
   * 根据输入和方法操作服，推断输出类型.
   */
  public static DataType functionResultSpeculate(FunctionRef functionRef, RowHeader rowHeader) {
    Class<?> returnType = FunctionUtils.checkFunctionType(functionRef, rowHeader);
    return convertFromJavaType(returnType);
  }

  /**
   * 从java类型转换为数据类型.
   */
  public static DataType convertFromJavaType(Class<?> type) {
    if (type.equals(Integer.class)) {
      return new IntType();
    } else if (type.equals(Long.class)) {
      return new BigIntType();
    } else if (type.equals(String.class)) {
      return new VarcharType();
    } else if (type.equals(Boolean.class)) {
      return new BooleanType();
    } else if (type.equals(LocalDate.class)) {
      return new DateType();
    } else if (type.equals(Double.class)) {
      return new DoubleType();
    }
    throw new AstConvertorException("[%s]无法转换到数据类型", type);
  }

  /**
   * 从java类型转换为数据类型.
   */
  public static DataType convertFromPlain(String type) {
    switch (type.toUpperCase()) {
      case "INTEGER":
        return new IntType();
      case "LONG":
        return new BigIntType();
      case "STRING":
        return new VarcharType();
      case "BOOLEAN":
        return new BooleanType();
      case "DATE":
        return new DateType();
      default:
        throw new AstConvertorException("[%s]无法转换到数据类型", type);
    }
  }
}
