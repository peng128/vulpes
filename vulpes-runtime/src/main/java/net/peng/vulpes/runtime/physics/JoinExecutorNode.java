package net.peng.vulpes.runtime.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.logical.RelalgJoin;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.exchange.ExchangeService;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ExchangeInputExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.Row;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.util.TransferPair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description of JoinExecutorNode.
 * 只支持join条件使用AND连接，并且join的条件是等于，且不带任何函数计算的等于.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/14
 */
public class JoinExecutorNode extends ExchangeInputExecutorNode {

  private final RelalgJoin.JoinType joinType;
  private final RelalgJoin.JoinSide joinSide;
  private final List<Pair<ColumnNameExpr, ColumnNameExpr>> condition;

  /**
   * Join执行节点.
   */
  public JoinExecutorNode(ExecutorNode next, RowHeader outputRowHeader,
                          RelalgJoin.JoinType joinType, RelalgJoin.JoinSide joinSide,
                          List<Pair<ColumnNameExpr, ColumnNameExpr>> condition) {
    super(next, outputRowHeader);
    this.joinType = joinType;
    this.joinSide = joinSide;
    this.condition = condition;
  }

  private ExchangeService getLeftDataFetcher() {
    return exchangeServiceList.get(0);
  }

  private ExchangeService getRightDataFetcher() {
    return exchangeServiceList.get(1);
  }

  @Override
  public Segment<?> fetchData(MemorySpace memorySpace) {
    final Segment<?> leftData = getLeftDataFetcher().fetch(memorySpace);
    final Segment<?> rightData = getRightDataFetcher().fetch(memorySpace);
    if (leftData instanceof ArrowSegment && rightData instanceof ArrowSegment) {
      return new ArrowSegment(processJoin((ArrowSegment) leftData, (ArrowSegment) rightData,
              memorySpace));
    }
    throw new ComputeException("不认识数据内存格式[%s][%s]", leftData.get().getClass().getName(),
            rightData.get().getClass().getName());
  }

  private List<VectorSchemaRoot> processJoin(ArrowSegment leftSegment, ArrowSegment rightSegment,
                                             MemorySpace memorySpace) {
    List<Integer> leftIndex = new ArrayList<>();
    List<Integer> rightIndex = new ArrayList<>();
    for (Pair<ColumnNameExpr, ColumnNameExpr> conditionPair : condition) {
      Integer tableIndex = outputRowHeader.aliasIndexOf(conditionPair.getLeft().getQualifier(),
              conditionPair.getLeft().getName());
      if (tableIndex == -1) {
        throw new ComputeException("找不到这个列[%s].%s", conditionPair.getLeft(), outputRowHeader);
      }
      if (tableIndex == 1) {
        leftIndex.add(outputRowHeader.aliasInternalIndexOf(conditionPair.getLeft().getQualifier(),
                conditionPair.getLeft().getName()));
        rightIndex.add(outputRowHeader.aliasInternalIndexOf(conditionPair.getRight().getQualifier(),
                conditionPair.getRight().getName()));
      } else {
        leftIndex.add(outputRowHeader.aliasInternalIndexOf(conditionPair.getRight().getQualifier(),
                conditionPair.getRight().getName()));
        rightIndex.add(outputRowHeader.aliasInternalIndexOf(conditionPair.getLeft().getQualifier(),
                conditionPair.getLeft().getName()));
      }
    }
    if (joinSide.equals(RelalgJoin.JoinSide.FULL)
            && joinType.equals(RelalgJoin.JoinType.OUTER_JOIN)) {
      //TODO FULL OUTER JOIN的支持.
      throw new ComputeException("暂时不支持FULL OUTER JOIN");
    } else if (joinSide.equals(RelalgJoin.JoinSide.RIGHT)) {
      return internalProcess(rightSegment.get(), leftSegment.get(), rightIndex, leftIndex,
              joinType.equals(RelalgJoin.JoinType.INNER_JOIN), memorySpace, outputRowHeader);
    }
    return internalProcess(leftSegment.get(), rightSegment.get(), leftIndex, rightIndex,
            joinType.equals(RelalgJoin.JoinType.INNER_JOIN), memorySpace, outputRowHeader);
  }

  private static List<VectorSchemaRoot> internalProcess(List<VectorSchemaRoot> leftData,
                                                        List<VectorSchemaRoot> rightData,
                                                        List<Integer> leftColumnIndex,
                                                        List<Integer> rightColumnIndex,
                                                        boolean inner,
                                                        MemorySpace memorySpace,
                                                        RowHeader outputRowHeader) {
    List<VectorSchemaRoot> result = new ArrayList<>();
    for (VectorSchemaRoot leftDatum : leftData) {
      result.add(internalProcess2(leftDatum, rightData, leftColumnIndex, rightColumnIndex, inner,
              memorySpace, outputRowHeader));
    }
    try {
      AutoCloseables.close(leftData);
      AutoCloseables.close(rightData);
    } catch (Exception e) {
      throw new ComputeException("内存回收失败.", e);
    }
    return result;
  }

  private static VectorSchemaRoot internalProcess2(VectorSchemaRoot leftData,
                                                   List<VectorSchemaRoot> rightData,
                                                   List<Integer> leftColumnIndex,
                                                   List<Integer> rightColumnIndex, boolean inner,
                                                   MemorySpace memorySpace,
                                                   RowHeader outputRowHeader) {
    final FieldVector[] fieldVectors = new FieldVector[outputRowHeader.getColumns().size()];
    int fieldIndex = 0;
    final Iterator<FieldVector> leftFieldVectorIterator =
            leftData.getFieldVectors().stream().map(fieldVector ->
                    (FieldVector) fieldVector.getTransferPair(memorySpace.getAllocator()).getTo())
                    .iterator();
    while (leftFieldVectorIterator.hasNext()) {
      fieldVectors[fieldIndex] = leftFieldVectorIterator.next();
      fieldIndex++;
    }

    final Iterator<FieldVector> rightFieldVectorIterator =
            rightData.get(0).getFieldVectors().stream().map(fieldVector ->
                    (FieldVector) fieldVector.getTransferPair(memorySpace.getAllocator()).getTo())
                    .iterator();
    while (rightFieldVectorIterator.hasNext()) {
      fieldVectors[fieldIndex] = rightFieldVectorIterator.next();
      fieldIndex++;
    }
    final LinkedHashMap<Integer, List<FieldVector>> matchedIndex = new LinkedHashMap<>();
    // 左表的缓存，用于降低左表重复数据的查询复杂度.
    final Map<Row, Integer> leftRowCache = new HashMap<>();
    int outputRowCount = 0;
    for (int i = 0; i < leftData.getRowCount(); i++) {
      final int thisIndex = i;
      final Row row = new Row(leftColumnIndex.stream()
              .map(index -> leftData.getVector(index).getObject(thisIndex)).toList());
      int cachedRowIndex = leftRowCache.getOrDefault(row, -1);
      List<FieldVector> matchedRight;
      if (cachedRowIndex > 0) {
        matchedRight = matchedIndex.get(cachedRowIndex);
      } else {
        matchedRight = rowJoinProcess(row, rightData, rightColumnIndex, memorySpace);
      }
      //如果不是inner join，就要把右表时空的情况输出.
      if (getRowCount(matchedRight) == 0 && inner) {
        matchedRight.forEach(FieldVector::close);
        continue;
      } else if (getRowCount(matchedRight) == 0) {
        // 如果是outer join，那么需要将输出行数加一.
        outputRowCount++;
      }
      matchedIndex.put(i, matchedRight);
      outputRowCount += getRowCount(matchedRight);
    }
    if (ObjectUtils.isEmpty(matchedIndex)) {
      return null;
    }
    // 初始化内存
    for (FieldVector fieldVector : fieldVectors) {
      fieldVector.setInitialCapacity(outputRowCount);
      fieldVector.reAlloc();
    }
    // 赋值
    int resultRowCount = 0;
    for (Integer i : matchedIndex.keySet()) {
      List<FieldVector> matchedVectors = matchedIndex.get(i);
      if (getRowCount(matchedVectors) == 0) {
        setRightNull(fieldVectors, leftData.getFieldVectors(), matchedVectors, resultRowCount, i);
        matchedVectors.forEach(ValueVector::close);
        resultRowCount++;
        continue;
      }
      for (int j = 0; j < getRowCount(matchedVectors); j++) {
        setData(fieldVectors, leftData.getFieldVectors(), matchedVectors, resultRowCount, i, j);
        resultRowCount++;
      }
      matchedVectors.forEach(ValueVector::close);
    }
    for (FieldVector fieldVector : fieldVectors) {
      fieldVector.setValueCount(outputRowCount);
    }
    return VectorSchemaRoot.of(fieldVectors);
  }

  private static void setData(FieldVector[] resultVectors, List<FieldVector> leftVectors,
                              List<FieldVector> matchVectors, Integer thisIndex,
                              Integer leftIndex, Integer matchIndex) {
    for (int j = 0; j < leftVectors.size(); j++) {
      resultVectors[j].copyFromSafe(leftIndex, thisIndex, leftVectors.get(j));
    }
    for (int j = 0; j < matchVectors.size(); j++) {
      resultVectors[j + leftVectors.size()].copyFromSafe(matchIndex, thisIndex,
              matchVectors.get(j));
    }
  }

  private static void setRightNull(FieldVector[] resultVectors, List<FieldVector> leftVectors,
                                   List<FieldVector> matchVectors, Integer thisIndex,
                                   Integer leftIndex) {
    for (int j = 0; j < leftVectors.size(); j++) {
      resultVectors[j].copyFromSafe(leftIndex, thisIndex, leftVectors.get(j));
    }
    for (int j = 0; j < matchVectors.size(); j++) {
      resultVectors[j + leftVectors.size()].setNull(thisIndex);
    }
  }

  private static List<FieldVector> rowJoinProcess(Row row, List<VectorSchemaRoot> rightData,
                                                  List<Integer> rightColumnIndex,
                                                  MemorySpace memorySpace) {
    List<FieldVector> matchedRight = null;
    for (VectorSchemaRoot rightDatum : rightData) {
      List<Integer> filterIndexes = rowJoinProcessSingle(row, rightDatum, rightColumnIndex);
      List<FieldVector> fieldVectorList = filterByIndex(filterIndexes, rightDatum, memorySpace);
      if (ObjectUtils.isNull(matchedRight)) {
        matchedRight = fieldVectorList;
      } else if (!ObjectUtils.isEmpty(filterIndexes)) {
        matchedRight = combine(matchedRight, fieldVectorList, memorySpace);
      } else {
        fieldVectorList.forEach(FieldVector::close);
      }
    }
    return matchedRight;
  }

  private static List<FieldVector> filterByIndex(List<Integer> indexes, VectorSchemaRoot data,
                                                 MemorySpace memorySpace) {
    final List<TransferPair> transferPairs =
            data.getFieldVectors().stream().map(fieldVector ->
                    fieldVector.getTransferPair(memorySpace.getAllocator())).toList();
    for (TransferPair transferPair : transferPairs) {
      transferPair.getTo().setInitialCapacity(indexes.size());
      transferPair.getTo().reAlloc();
    }
    for (int i = 0; i < indexes.size(); i++) {
      final int index = i;
      transferPairs.forEach(transferPair -> transferPair.copyValueSafe(indexes.get(index), index));
    }
    return transferPairs.stream().map(transferPair -> {
      transferPair.getTo().setValueCount(indexes.size());
      return (FieldVector) transferPair.getTo();
    }).toList();
  }

  /**
   * 将两个向量列表合并，并清除原先列表存储.
   */
  private static List<FieldVector> combine(List<FieldVector> a, List<FieldVector> b,
                                           MemorySpace memorySpace) {
    if (a.size() != b.size()) {
      throw new ComputeException("两个fieldVector相加时字段不同.");
    }
    if (getRowCount(a) == 0 && getRowCount(b) > 0) {
      a.forEach(FieldVector::close);
      return b;
    } else if (getRowCount(a) > 0 && getRowCount(b) == 0) {
      b.forEach(FieldVector::close);
      return a;
    } else if (getRowCount(a) == 0 && getRowCount(b) == 0) {
      b.forEach(FieldVector::close);
      return a;
    }
    final List<FieldVector> fieldVectors = a.stream()
            .map(fieldVector -> (FieldVector) fieldVector
                    .getTransferPair(memorySpace.getAllocator())
                    .getTo()).toList();
    fieldVectors.forEach(transferPair -> {
      transferPair.setInitialCapacity(getRowCount(a) + getRowCount(b));
      transferPair.reAlloc();
    });
    for (int i = 0; i < getRowCount(a); i++) {
      for (int j = 0; j < fieldVectors.size(); j++) {
        fieldVectors.get(j).copyFrom(i, i, a.get(j));
      }
    }
    for (int i = 0; i < getRowCount(b); i++) {
      for (int j = 0; j < fieldVectors.size(); j++) {
        fieldVectors.get(j).copyFrom(i, i + getRowCount(a), b.get(j));
      }
    }
    fieldVectors.forEach(fieldVector ->
            fieldVector.setValueCount(getRowCount(a) + getRowCount(b)));
    a.forEach(FieldVector::close);
    b.forEach(FieldVector::close);
    return fieldVectors;
  }

  private static List<Integer> rowJoinProcessSingle(Row row, VectorSchemaRoot rightData,
                                                    List<Integer> rightColumnIndex) {
    final List<Integer> matchIndexes = new ArrayList<>();
    final List<FieldVector> vectors = rightColumnIndex.stream().map(rightData::getVector).toList();
    for (int i = 0; i < rightData.getRowCount(); i++) {
      for (int j = 0; j < vectors.size(); j++) {
        if (row.getData().get(j).equals(vectors.get(j).getObject(i))) {
          matchIndexes.add(i);
        }
      }
    }
    return matchIndexes;
  }

  private static Integer getRowCount(List<FieldVector> fieldVectors) {
    return fieldVectors.get(0).getValueCount();
  }
}
