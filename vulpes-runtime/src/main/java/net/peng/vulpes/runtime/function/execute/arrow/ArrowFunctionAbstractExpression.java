package net.peng.vulpes.runtime.function.execute.arrow;

import java.util.ArrayList;
import java.util.List;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.function.Function;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.LiteralExpr;
import net.peng.vulpes.parser.algebraic.expression.NumericExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.function.execute.ExpressionExecutor;
import net.peng.vulpes.runtime.memory.MemorySpace;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.ValueVector;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description of ArrowFunctionAbstractExpression.
 * TODO: 这里函数调用可以从反射改为代码生成。反射invoke会消耗cpu。换成代码生成会更快.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/12
 */
public abstract class ArrowFunctionAbstractExpression<R>
    implements ExpressionExecutor<List<FieldVector>, R> {

  private static final int DEFAULT_ROW_COUNT = 1;

  protected final FunctionRef functionRef;

  protected final RowHeader inputRowHeader;

  protected ArrowFunctionAbstractExpression(FunctionRef functionRef, RowHeader inputRowHeader) {
    this.functionRef = functionRef;
    this.inputRowHeader = inputRowHeader;
  }

  @Override
  public R execute(List<FieldVector> data, MemorySpace memorySpace) {
    checkFunctionType(functionRef.getFunction());

    return internalExecute(data, memorySpace);
  }

  /**
   * 检测函数类型是否适应于此次执行.
   */
  protected abstract boolean checkFunctionType(Function function);

  /**
   * 应用函数的计算逻辑.
   *
   * @param itemResult 计算后的数据
   * @param columnInfo 结果字段信息
   * @param rowCount   总行数
   * @param data       原始数据
   */
  protected abstract R computeResultByFunction(
      Pair<List<FieldVector>, List<Integer>> itemResult, ColumnInfo columnInfo,
      int rowCount, List<FieldVector> data, MemorySpace memorySpace);

  private R internalExecute(List<FieldVector> data, MemorySpace memorySpace) {

    Pair<List<FieldVector>, List<Integer>> itemResult = computeFunctionItems(data, memorySpace);
    // 创建结果数据并申请内存.
    ColumnInfo columnInfo = functionRef.fillColumnInfo(inputRowHeader);
    int rowCount = DEFAULT_ROW_COUNT;
    if (!ObjectUtils.isEmpty(data)) {
      rowCount = data.get(0).getValueCount();
    }
    final R result = computeResultByFunction(itemResult, columnInfo, rowCount,
        data, memorySpace);
    itemResult.getLeft().forEach(ValueVector::close);
    return result;
  }

  /**
   * 找到函数需要的数据.
   *
   * @param data       原始数据
   * @param itemResult key为计算后的字段（函数嵌套的场景）value是原始数据的列索引，
   *                   如果大于原始数据的最大列数，则为计算后字段.
   * @param rowIndex   行号.
   * @return 函数涉及到的行数据.
   */
  protected Object[] getElementData(List<FieldVector> data, Pair<List<FieldVector>,
      List<Integer>> itemResult, int rowIndex) {
    List<FieldVector> extraFunctionData = itemResult.getLeft();
    List<Integer> involvedColumnIndexes = itemResult.getRight();
    Object[] elements = new Object[involvedColumnIndexes.size()];
    for (int i = 0; i < involvedColumnIndexes.size(); i++) {
      if (involvedColumnIndexes.get(i) >= data.size()) {
        elements[i] = extraFunctionData.get(involvedColumnIndexes.get(i) - data.size())
            .getObject(rowIndex);
      } else {
        elements[i] = data.get(involvedColumnIndexes.get(i)).getObject(rowIndex);
      }
    }
    return elements;
  }

  //TODO 这里设计的不好，这里如果是嵌套函数就先计算内层函数的所有行，这里可以改成每行就计算一次.
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
