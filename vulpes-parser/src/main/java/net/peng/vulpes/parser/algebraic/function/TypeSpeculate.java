package net.peng.vulpes.parser.algebraic.function;

import java.util.Arrays;
import java.util.List;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * Description of TypeSpeculate.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/9
 */
public class TypeSpeculate {

  private DataType dataType;

  /**
   * 和输入字段相同的的类型.
   */
  private final Integer inputColumnIndex;

  public TypeSpeculate(DataType dataType, Integer inputColumnIndex) {
    this.dataType = dataType;
    this.inputColumnIndex = inputColumnIndex;
  }

  public TypeSpeculate(DataType dataType) {
    this.dataType = dataType;
    this.inputColumnIndex = -1;
  }

  public TypeSpeculate(Integer inputColumnIndex) {
    this.inputColumnIndex = inputColumnIndex;
  }

  public static TypeSpeculate create(Integer inputColumnIndex) {
    return new TypeSpeculate(inputColumnIndex);
  }

  public static TypeSpeculate create(DataType dataType) {
    return new TypeSpeculate(dataType);
  }

  /**
   * 根据输入类型构建推断.
   */
  public TypeSpeculate build(List<DataType> dataTypes) {
    if (ObjectUtils.isNotNull(dataType)) {
      return this;
    }
    if (dataTypes.size() < inputColumnIndex) {
      throw new ComputeException("字段推断出错, 无法在【%s】中找到第【%s】个字段类型。", dataTypes, inputColumnIndex);
    }
    dataType = dataTypes.get(inputColumnIndex);
    return this;
  }

  /**
   * 获取字段推断类型.
   */
  public DataType getDataType() {
    if (ObjectUtils.isNull(dataType)) {
      throw new ComputeException("未初始化字段推断类型.");
    }
    return dataType;
  }
}
