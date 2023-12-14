package net.peng.vulpes.runtime.function.execute.arrow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.function.Function;
import net.peng.vulpes.common.function.aggregate.AggregateFunction;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.FunctionUtils;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.Row;
import net.peng.vulpes.runtime.struct.map.DistributeMap;
import org.apache.arrow.vector.FieldVector;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description of ArrowAggregateExpression.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/13
 */
public class ArrowAggregateExpression extends ArrowFunctionAbstractExpression<Void> {

  private static final String INIT_STATE_FUNCTION_NAME = "init";

  private static final String MERGE_STATE_FUNCTION_NAME = "merge";

  //TODO 这里状态的内存也需要管理一下.
  private final DistributeMap<Row, Function[]> stateMap;

  private final List<Integer> groupByIndexes;

  /**
   * 总共有多少个聚合函数.
   */
  private final Integer totalFunctionNum;

  /**
   * 这个聚合函数的索引.
   */
  private final Integer thisFunctionIndex;

  private final Method mergeMethod;

  /**
   * 聚合表达式执行器.
   */
  public ArrowAggregateExpression(FunctionRef functionRef, RowHeader inputRowHeader,
                                  DistributeMap<Row, Function[]> stateMap,
                                  List<Integer> groupByIndexes,
                                  Integer totalFunctionNum, Integer thisFunctionIndex) {
    super(functionRef, inputRowHeader);
    this.stateMap = stateMap;
    this.groupByIndexes = groupByIndexes;
    this.totalFunctionNum = totalFunctionNum;
    this.thisFunctionIndex = thisFunctionIndex;
    this.mergeMethod = getMergeMethod(functionRef.getFunction());
  }

  @Override
  protected boolean checkFunctionType(Function function) {
    if (!(function instanceof AggregateFunction)) {
      throw new ComputeException("聚合函数执行器只能执行聚合函数，但是输入为 %s", function);
    }
    return true;
  }

  @Override
  protected Void computeResultByFunction(Pair<List<FieldVector>, List<Integer>> itemResult,
                                         ColumnInfo columnInfo, int rowCount,
                                         List<FieldVector> data, MemorySpace memorySpace) {
    for (int i = 0; i < rowCount; i++) {
      List<Object> groupKey = new ArrayList<>(groupByIndexes.size());
      for (Integer groupByIndex : groupByIndexes) {
        groupKey.add(data.get(groupByIndex).getObject(i));
      }
      final Row key = new Row(groupKey);
      if (!stateMap.contains(key)) {
        stateMap.put(key, new Function[totalFunctionNum]);
      }
      Function[] functions = stateMap.get(key);
      if (ObjectUtils.isNull(functions[thisFunctionIndex])) {
        //初始化聚合状态.
        functions[thisFunctionIndex] = FunctionUtils.getFunction(functionRef.getOperator(),
            functionRef.getSessionManager().getClassLoader());
        try {
          FunctionUtils.getMethod(functionRef.getOperator(), functions[thisFunctionIndex],
              INIT_STATE_FUNCTION_NAME).invoke(functions[thisFunctionIndex]);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new ComputeException("无法执行聚合函数合并方法[%s].", e.getMessage(), e);
        }
      }
      try {
        mergeMethod.invoke(functions[thisFunctionIndex], getElementData(data, itemResult, i));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new ComputeException("无法执行聚合函数合并方法[%s].", e.getMessage(), e);
      }
    }
    return null;
  }

  /**
   * 获取合并函数执行方法.
   */
  private Method getMergeMethod(Function function) {
    Class<?>[] parameterType = new Class[functionRef.getItems().size()];
    for (int i = 0; i < functionRef.getItems().size(); i++) {
      parameterType[i] = functionRef.getItems().get(i).fillColumnInfo(inputRowHeader)
          .getDataType().getJavaType();
    }
    return FunctionUtils.getMethod(functionRef.getOperator(), function,
        MERGE_STATE_FUNCTION_NAME, parameterType);
  }

}
