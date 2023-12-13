package net.peng.vulpes.runtime.physics;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.function.Function;
import net.peng.vulpes.parser.algebraic.expression.AliasExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.FunctionUtils;
import net.peng.vulpes.runtime.function.execute.arrow.ArrowAggregateExpression;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ComputeExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.Row;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import net.peng.vulpes.runtime.struct.map.BucketDistributeMap;
import net.peng.vulpes.runtime.struct.map.DistributeMap;
import net.peng.vulpes.runtime.utils.VectorBuilderUtils;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of PreAggregateExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
@ToString
public class AggregateExecutorNode extends ComputeExecutorNode {

  private static final String GET_FUNCTION_NAME = "get";

  /**
   * 默认输出聚合函数计算后数据使用的字段别名.
   */
  private static final String AGG_FUNCTION_OUTPUT_NAME = "AGG_OUTPUT_%s";

  /**
   * 聚合列索引.
   */
  @Getter
  private final List<Integer> groupByIndex;

  /**
   * 聚合函数.
   */
  @Getter
  private List<RelalgExpr> aggFunctions;


  /**
   * 聚合算子执行节点.
   */
  public AggregateExecutorNode(ExecutorNode next, List<Integer> groupByIndex,
                               List<RelalgExpr> aggFunctions, RowHeader inputRowHeader,
                               RowHeader outputRowHeader) {
    super(next, inputRowHeader, outputRowHeader);
    this.groupByIndex = groupByIndex;
    this.aggFunctions = aggFunctions;
  }

  @Override
  public Segment<?> executeSingleInput(Segment<?> segment, MemorySpace memorySpace) {
    if (!(segment instanceof ArrowSegment)) {
      throw new ComputeException("不认识数据类型. %s", segment.getClass().getName());
    }
    List<VectorSchemaRoot> vectorSchemaRoots = ((ArrowSegment) segment).get();
    DistributeMap<Row, Function[]> stateMap =
        new BucketDistributeMap<>(vectorSchemaRoots.size());
    vectorSchemaRoots.forEach(vectorSchemaRoot -> {
      buildMiddleData(vectorSchemaRoot, memorySpace, stateMap);
      vectorSchemaRoot.close();
    });
    return new ArrowSegment(computeResult(stateMap, memorySpace));
  }

  private void buildMiddleData(VectorSchemaRoot vectorSchemaRoot, MemorySpace memorySpace,
                               DistributeMap<Row, Function[]> stateMap) {
    for (int j = 0; j < aggFunctions.size(); j++) {
      RelalgExpr relalgExpr = aggFunctions.get(j);
      if (relalgExpr instanceof AliasExpr aliasExpr) {
        relalgExpr = aliasExpr.getRelalgExpr();
      } else {
        // 如何聚合函数没有重命名，就重命名一下.
        outputRowHeader.getColumns().get(groupByIndex.size() + j)
            .setName(String.format(AGG_FUNCTION_OUTPUT_NAME, j));
      }
      if (!(relalgExpr instanceof FunctionRef functionRef)) {
        throw new ComputeException("聚合算子只能处理聚合函数.输入为[%s].", relalgExpr);
      }
      ArrowAggregateExpression arrowAggregateExpression =
          new ArrowAggregateExpression(functionRef, inputRowHeader, stateMap, groupByIndex,
              aggFunctions.size(), j);
      arrowAggregateExpression.execute(vectorSchemaRoot.getFieldVectors(), memorySpace);
    }
  }

  /**
   * 从中间状态获取最终结果.
   */
  private List<VectorSchemaRoot> computeResult(DistributeMap<Row, Function[]> stateMap,
                                               MemorySpace memorySpace) {
    List<VectorSchemaRoot> vectorSchemaRoots = new ArrayList<>();
    Iterator<List<Map.Entry<Row, Function[]>>> bucketIterator = stateMap.fetchAll();
    while (bucketIterator.hasNext()) {
      List<Map.Entry<Row, Function[]>> dataList = bucketIterator.next();
      List<FieldVector> outputVectors = new ArrayList<>();
      for (Integer byIndex : groupByIndex) {
        final FieldVector fieldVector = VectorBuilderUtils.buildFiledVector(
            inputRowHeader.getColumns().get(byIndex), memorySpace);
        fieldVector.setInitialCapacity(dataList.size());
        fieldVector.reAlloc();
        outputVectors.add(fieldVector);
      }
      for (int i = groupByIndex.size(); i < aggFunctions.size() + groupByIndex.size(); i++) {
        final FieldVector fieldVector = VectorBuilderUtils.buildFiledVector(
            outputRowHeader.getColumns().get(i), memorySpace);
        fieldVector.setInitialCapacity(dataList.size());
        fieldVector.reAlloc();
        outputVectors.add(fieldVector);
      }
      for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++) {
        Map.Entry<Row, Function[]> rowListEntry = dataList.get(rowIndex);
        int columnCount = 0;
        for (Object datum : rowListEntry.getKey().getData()) {
          VectorBuilderUtils.setFieldVector(outputRowHeader.getColumns().get(columnCount),
              outputVectors.get(columnCount), rowIndex, datum);
          columnCount++;
        }
        for (Function function : rowListEntry.getValue()) {
          Object result;
          try {
            result = FunctionUtils.getMethod(function.getClass().getName(), function,
                GET_FUNCTION_NAME).invoke(function);
          } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ComputeException("无法执行聚合函数获取结果方法[%s].", e.getMessage(), e);
          }
          VectorBuilderUtils.setFieldVector(outputRowHeader.getColumns().get(columnCount),
              outputVectors.get(columnCount), rowIndex, result);
          columnCount++;
        }
      }
      outputVectors.forEach(fieldVector -> fieldVector.setValueCount(dataList.size()));
      vectorSchemaRoots.add(VectorSchemaRoot.of(outputVectors.toArray(new FieldVector[0])));
    }
    return vectorSchemaRoots;
  }
}
