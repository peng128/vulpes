package net.peng.vulpes.runtime.function.execute.arrow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import lombok.ToString;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.function.Function;
import net.peng.vulpes.common.function.scalar.ScalarFunction;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.FunctionUtils;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.utils.VectorBuilderUtils;
import org.apache.arrow.vector.FieldVector;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description of ArrowScalarExpression.
 * 标量函数表达式执行器.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/28
 */
@ToString
public class ArrowScalarExpression extends ArrowFunctionAbstractExpression<FieldVector> {

  public final Method method;

  public ArrowScalarExpression(FunctionRef functionRef, RowHeader inputRowHeader) {
    super(functionRef, inputRowHeader);
    method = getScalarMethod();
  }

  @Override
  protected boolean checkFunctionType(Function function) {
    if (!(function instanceof ScalarFunction)) {
      throw new ComputeException("标量函数执行器只能执行标量函数，但是输入为", function);
    }
    return true;
  }

  @Override
  protected FieldVector computeResultByFunction(
      Pair<List<FieldVector>, List<Integer>> itemResult, ColumnInfo columnInfo,
      int rowCount, List<FieldVector> data, MemorySpace memorySpace) {
    FieldVector result = initResultVector(rowCount, columnInfo, memorySpace);
    // 计算
    for (int i = 0; i < rowCount; i++) {
      try {
        Object resultData =
            method.invoke(functionRef.getFunction(), getElementData(data, itemResult, i));
        VectorBuilderUtils.setFieldVector(columnInfo, result, i, resultData);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new ComputeException("无法执行标量方法. %s", e, functionRef);
      }
    }
    result.setValueCount(rowCount);
    return result;
  }

  /**
   * 初始化结果{@link FieldVector}.
   */
  private FieldVector initResultVector(int rowCount, ColumnInfo columnInfo,
                                                  MemorySpace memorySpace) {
    FieldVector fieldVector = VectorBuilderUtils.buildFiledVector(columnInfo, memorySpace);
    fieldVector.setInitialCapacity(rowCount);
    fieldVector.reAlloc();
    return fieldVector;
  }

  /**
   * 获取标量函数执行方法.
   */
  private Method getScalarMethod() {
    Class<?>[] parameterType = new Class[functionRef.getItems().size()];
    for (int i = 0; i < functionRef.getItems().size(); i++) {
      parameterType[i] = functionRef.getItems().get(i).fillColumnInfo(inputRowHeader)
          .getDataType().getJavaType();
    }
    return FunctionUtils.getEvalMethod(functionRef.getOperator(),
        functionRef.getFunction(), parameterType);
  }
}
