package net.peng.vulpes.runtime.physics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.function.MiddleState;
import net.peng.vulpes.common.function.aggregate.AggregateFunction;
import net.peng.vulpes.common.function.aggregate.SumFunction;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ComputeExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.Row;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import net.peng.vulpes.runtime.struct.map.BucketDistributeMap;
import net.peng.vulpes.runtime.struct.map.DistributeMap;
import net.peng.vulpes.runtime.utils.VectorBuilderUtils;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.util.TransferPair;

/**
 * Description of PreAggregateExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
@ToString
public class AggregateExecutorNode extends ComputeExecutorNode {

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
  private final List<AggregateFunction> aggFunctions;


  /**
   * 聚合算子执行节点.
   */
  public AggregateExecutorNode(ExecutorNode next, List<Integer> groupByIndex,
                               List<AggregateFunction> aggFunctions, RowHeader inputRowHeader,
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
    DistributeMap<Row, List<MiddleState>> middleData =
            new BucketDistributeMap<>(vectorSchemaRoots.size());
    vectorSchemaRoots.forEach(vectorSchemaRoot -> {
      buildMiddleData(vectorSchemaRoot, middleData);
      vectorSchemaRoot.close();
    });
    return new ArrowSegment(buildData(middleData, memorySpace));
  }

  private void buildMiddleData(VectorSchemaRoot vectorSchemaRoot,
                               DistributeMap<Row, List<MiddleState>> middleData) {
    List<FieldVector> fieldVectors =
            groupByIndex.stream().map(vectorSchemaRoot::getVector).toList();
    for (int i = 0; i < vectorSchemaRoot.getRowCount(); i++) {
      final int index = i;
      List<Object> groupKey =
              fieldVectors.stream().map(valueVectors -> valueVectors.getObject(index)).toList();
      final Row key = new Row(groupKey);
      if (!middleData.contains(key)) {
        List<MiddleState> states = new ArrayList<>(aggFunctions.size());
        for (int z = 0; z < aggFunctions.size(); z++) {
          states.add(z, aggFunctions.get(z).initState());
        }
        middleData.put(key, states);
      }
      List<MiddleState> states = middleData.get(key);
      for (int j = 0; j < aggFunctions.size(); j++) {
        if (aggFunctions.get(j) instanceof SumFunction) {
          SumFunction sumFunction = (SumFunction) aggFunctions.get(j);
          final Integer rowNum = i;
          Long value = sumFunction.getInputColumnIndex().stream()
                  .map(columnIndex -> (Long) vectorSchemaRoot.getVector(columnIndex)
                          .getObject(rowNum)).mapToLong(Long::longValue).sum();
          states.get(j).merge(value);
        }
      }
    }
  }

  private List<VectorSchemaRoot> buildData(DistributeMap<Row, List<MiddleState>> middleData,
                                     MemorySpace memorySpace) {
    List<VectorSchemaRoot> vectorSchemaRoots = new ArrayList<>();
    Iterator<List<Map.Entry<Row, List<MiddleState>>>> bucketIterator = middleData.fetchAll();
    while (bucketIterator.hasNext()) {
      List<Map.Entry<Row, List<MiddleState>>> dataList = bucketIterator.next();
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
        Map.Entry<Row, List<MiddleState>> rowListEntry = dataList.get(rowIndex);
        int columnCount = 0;
        for (Object datum : rowListEntry.getKey().getData()) {
          VectorBuilderUtils.setFieldVector(outputRowHeader.getColumns().get(columnCount),
                  outputVectors.get(columnCount), rowIndex, datum);
          columnCount++;
        }
        for (MiddleState middleState : rowListEntry.getValue()) {
          VectorBuilderUtils.setFieldVector(outputRowHeader.getColumns().get(columnCount),
                  outputVectors.get(columnCount), rowIndex, middleState.get());
          columnCount++;
        }
      }
      outputVectors.forEach(fieldVector -> fieldVector.setValueCount(dataList.size()));
      vectorSchemaRoots.add(VectorSchemaRoot.of(outputVectors.toArray(new FieldVector[0])));
    }
    return vectorSchemaRoots;
  }

  private VectorSchemaRoot internalExecute(VectorSchemaRoot vectorSchemaRoot,
                                           MemorySpace memorySpace) {
    Map<List<Object>, List<Integer>> rowIndex = new LinkedHashMap<>();
    List<FieldVector> fieldVectors =
            groupByIndex.stream().map(vectorSchemaRoot::getVector).toList();
    for (int i = 0; i < vectorSchemaRoot.getRowCount(); i++) {
      final int index = i;
      List<Object> groupKey =
              fieldVectors.stream().map(valueVectors -> valueVectors.getObject(index)).toList();
      List<Integer> groupKeyIndex = rowIndex.getOrDefault(groupKey, new ArrayList<>());
      groupKeyIndex.add(index);
      rowIndex.put(groupKey, groupKeyIndex);
    }
    List<TransferPair> transferPairs =
            fieldVectors.stream().map(x -> x.getTransferPair(memorySpace.getAllocator())).toList();
    transferPairs.forEach(x -> {
      x.getTo().setInitialCapacity(rowIndex.size());
      x.getTo().reAlloc();
    });
    //TODO 这里先写死一个sum，之后抽样为接口
    List<FieldVector> aggOutputColumns = new ArrayList<>(aggFunctions.size());
    int count = fieldVectors.size();
    for (AggregateFunction aggFunction : aggFunctions) {
      if (aggFunction instanceof SumFunction) {
        SumFunction sumFunction = (SumFunction) aggFunctions.get(0);
        String outputName = sumFunction.getOutputName();
        if (ObjectUtils.isNull(outputName)) {
          outputName = String.format(AGG_FUNCTION_OUTPUT_NAME, count);
        }
        FieldVector aggregateFieldVector = new BigIntVector(outputName, memorySpace.getAllocator());
        aggregateFieldVector.setInitialCapacity(rowIndex.size());
        aggregateFieldVector.reAlloc();
        aggOutputColumns.add(aggregateFieldVector);
      }
      count++;
    }
    int outputCount = 0;
    for (List<Integer> indexes : rowIndex.values()) {
      transferPairs.forEach(transferPair -> transferPair.copyValueSafe(indexes.get(0),
              indexes.get(0)));
      for (int i = 0; i < aggFunctions.size(); i++) {
        if (aggFunctions.get(i) instanceof SumFunction) {
          SumFunction sumFunction = (SumFunction) aggFunctions.get(0);
          long result = 0L;
          for (Integer index : indexes) {
            for (Integer inputColumnIndex : sumFunction.getInputColumnIndex()) {
              FieldVector inputVector = vectorSchemaRoot.getVector(inputColumnIndex);
              if (inputVector instanceof BigIntVector) {
                result += ((BigIntVector) inputVector).getObject(index);
              } else if (inputVector instanceof IntVector) {
                result += ((IntVector) inputVector).getObject(index);
              } else {
                throw new ComputeException("sum不支持这个【%s】类型的计算", inputVector.getClass().getName());
              }
            }
          }
          ((BigIntVector) aggOutputColumns.get(i)).set(outputCount, result);
        }
      }
      outputCount++;
    }
    List<FieldVector> outputVector =
            new ArrayList<>(transferPairs.size() + aggOutputColumns.size() + 1);
    for (TransferPair transferPair : transferPairs) {
      FieldVector fieldVector = (FieldVector) transferPair.getTo();
      fieldVector.setValueCount(rowIndex.size());
      outputVector.add(fieldVector);
    }
    for (FieldVector aggOutputColumn : aggOutputColumns) {
      aggOutputColumn.setValueCount(rowIndex.size());
      outputVector.add(aggOutputColumn);
    }
    vectorSchemaRoot.close();
    return new VectorSchemaRoot(outputVector);
  }
}
