package net.peng.vulpes.runtime.function.execute.arrow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.ToString;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.function.scalar.ScalarFunction;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.LiteralExpr;
import net.peng.vulpes.parser.algebraic.expression.NumericExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.FunctionUtils;
import net.peng.vulpes.runtime.function.execute.ExpressionExecutor;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.utils.VectorBuilderUtils;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.ValueVector;
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
public class ArrowScalarExpression implements ExpressionExecutor<List<FieldVector>, FieldVector> {

  private static final int DEFAULT_ROW_COUNT = 1;

  private final FunctionRef functionRef;

  private final RowHeader inputRowHeader;

  public ArrowScalarExpression(FunctionRef functionRef, RowHeader inputRowHeader) {
    this.functionRef = functionRef;
    this.inputRowHeader = inputRowHeader;
  }

  @Override
  public FieldVector execute(List<FieldVector> data, MemorySpace memorySpace) {
    if (!(functionRef.getFunction() instanceof ScalarFunction)) {
      throw new ComputeException("标量函数执行器只能执行标量函数，但是输入为", functionRef.getFunction());
    }
    Class<?>[] parameterType = new Class[functionRef.getItems().size()];
    for (int i = 0; i < functionRef.getItems().size(); i++) {
      parameterType[i] = functionRef.getItems().get(i).fillColumnInfo(inputRowHeader)
              .getDataType().getJavaType();
    }
    Method targetMethod = FunctionUtils.getEvalMethod(functionRef.getOperator(),
            functionRef.getFunction(), parameterType);
    return internalExecute(data, memorySpace, targetMethod);
  }

  private FieldVector internalExecute(List<FieldVector> data, MemorySpace memorySpace,
                                      Method method) {

    Pair<List<FieldVector>, List<Integer>> itemResult = computeFunctionItems(data, memorySpace);
    // 创建结果数据并申请内存.
    ColumnInfo columnInfo = functionRef.fillColumnInfo(inputRowHeader);
    FieldVector fieldVector = VectorBuilderUtils.buildFiledVector(columnInfo, memorySpace);
    int rowCount = DEFAULT_ROW_COUNT;
    if (!ObjectUtils.isEmpty(data)) {
      rowCount = data.get(0).getValueCount();
    }
    fieldVector.setInitialCapacity(rowCount);
    fieldVector.reAlloc();
    // 计算
    for (int i = 0; i < rowCount; i++) {
      try {
        Object result =
                method.invoke(functionRef.getFunction(), getElementData(data, itemResult, i));
        VectorBuilderUtils.setFieldVector(columnInfo, fieldVector, i, result);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new ComputeException("无法执行标量方法. %s", functionRef, e);
      }
    }
    fieldVector.setValueCount(rowCount);
    itemResult.getLeft().forEach(ValueVector::close);
    return fieldVector;
  }

  private Object[] getElementData(List<FieldVector> data, Pair<List<FieldVector>,
          List<Integer>> itemResult, int rowIndex) {
    List<FieldVector> extraFunctionData = itemResult.getLeft();
    List<Integer> involvedColumnIndexes = itemResult.getRight();
    Object[] elements = new Object[involvedColumnIndexes.size()];
    for (int i = 0; i < involvedColumnIndexes.size(); i++) {
      if (involvedColumnIndexes.get(i) >= data.size()) {
        elements[i] = extraFunctionData.get(involvedColumnIndexes.get(i) - data.size())
                .getObject(rowIndex);
      } else {
        elements[i] = data.get(involvedColumnIndexes.get(i))
                .getObject(rowIndex);
      }
    }
    return elements;
  }

  private Pair<List<FieldVector>, List<Integer>> computeFunctionItems(
          List<FieldVector> data, MemorySpace memorySpace) {
    int extraFunctionCount = 0;
    List<FieldVector> extraFunctionData = new ArrayList<>();
    List<Integer> involvedColumnIndexes = new ArrayList<>(functionRef.getItems().size());
    for (RelalgExpr item : functionRef.getItems()) {
      if (item instanceof ColumnNameExpr columnNameExpr) {
        involvedColumnIndexes.add(columnNameExpr.getIndex());
      } else if (item instanceof LiteralExpr literalExpr) {
        FieldVector fieldVector = new ArrowConstantExpression(literalExpr.getLiteral(),
                literalExpr.fillColumnInfo(inputRowHeader)).execute(data, memorySpace);
        extraFunctionData.add(fieldVector);
        involvedColumnIndexes.add(data.size() + extraFunctionCount);
        extraFunctionCount++;
      } else if (item instanceof NumericExpr numericExpr) {
        FieldVector fieldVector = new ArrowConstantExpression(numericExpr.getNumeric(),
                numericExpr.fillColumnInfo(inputRowHeader)).execute(data, memorySpace);
        extraFunctionData.add(fieldVector);
        involvedColumnIndexes.add(data.size() + extraFunctionCount);
        extraFunctionCount++;
      } else if (item instanceof FunctionRef subFunctionRef) {
        FieldVector fieldVector = subFunctionExecute(subFunctionRef, data, memorySpace);
        extraFunctionData.add(fieldVector);
        involvedColumnIndexes.add(data.size() + extraFunctionCount);
        extraFunctionCount++;
      } else {
        throw new ComputeException("无法处理函数中的表达式: %s", item);
      }
    }
    return Pair.of(extraFunctionData, involvedColumnIndexes);
  }

  private FieldVector subFunctionExecute(FunctionRef subfunctionRef, List<FieldVector> data,
                                         MemorySpace memorySpace) {
    ArrowScalarExpression subArrowScalarExpression = new ArrowScalarExpression(subfunctionRef,
            inputRowHeader);
    return subArrowScalarExpression.execute(data, memorySpace);
  }
}
