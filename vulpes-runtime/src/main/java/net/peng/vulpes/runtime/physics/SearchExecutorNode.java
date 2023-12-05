package net.peng.vulpes.runtime.physics;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ComputeExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.algorithm.sort.DefaultVectorComparators;
import org.apache.arrow.algorithm.sort.VectorValueComparator;
import org.apache.arrow.vector.BaseIntVector;
import org.apache.arrow.vector.BitVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.util.TransferPair;

/**
 * Description of FilterExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/18
 */
@Slf4j
@ToString(callSuper = true)
public class SearchExecutorNode extends ComputeExecutorNode {

  private final String columnName;
  private final Integer columnIndex;
  private final List<Object> searchArguments;

  /**
   * 过滤执行节点.
   */
  public SearchExecutorNode(ExecutorNode next, String columnName, Integer columnIndex,
                            List<Object> searchArguments, RowHeader inputRowHeader,
                            RowHeader outputRowHeader) {
    super(next, inputRowHeader, outputRowHeader);
    this.columnName = columnName;
    this.columnIndex = columnIndex;
    this.searchArguments = searchArguments;
  }

  @Override
  public Segment<?> executeSingleInput(Segment<?> segment, MemorySpace memorySpace) {
    if (!(segment instanceof ArrowSegment)) {
      throw new ComputeException("不认识数据类型. %s", segment.getClass().getName());
    }
    List<VectorSchemaRoot> vectorSchemaRoots = ((ArrowSegment) segment).get();
    List<VectorSchemaRoot> result = new ArrayList<>();
    for (VectorSchemaRoot vectorSchemaRoot : vectorSchemaRoots) {
      VectorSchemaRoot filtered = internalExecute(vectorSchemaRoot, memorySpace);
      if (filtered.getRowCount() > 0) {
        result.add(filtered);
      } else {
        filtered.close();
      }
    }
    return new ArrowSegment(result);
  }

  private VectorSchemaRoot internalExecute(VectorSchemaRoot vectorSchemaRoot,
                                          MemorySpace memorySpace) {
    final ValueVector dataVector = vectorSchemaRoot.getVector(columnIndex);
    final List<FieldVector> fieldVectors = vectorSchemaRoot.getFieldVectors();
    final List<TransferPair> filteredTransferPair = fieldVectors.stream()
            .map(valueVector -> valueVector.getTransferPair(memorySpace.getAllocator()))
            .collect(Collectors.toList());
    //标记过滤
    BitVector bitVector = new BitVector("filter-index", memorySpace.getAllocator());
    bitVector.setInitialCapacity(vectorSchemaRoot.getRowCount());
    bitVector.reAlloc();
    VectorValueComparator<ValueVector> comparator =
            DefaultVectorComparators.createDefaultComparator(dataVector);
    ValueVector searchVector = buildSearchVector(dataVector, searchArguments, memorySpace);
    comparator.attachVectors(dataVector, searchVector);
    int count = 0;
    for (int i = 0; i < dataVector.getValueCount(); i++) {
      if (match(comparator, searchVector, i)) {
        bitVector.set(i, 1);
        count++;
      }
    }
    // 初始化内存空间，避免使用safe方法导致内存占用过多
    for (TransferPair transferPair : filteredTransferPair) {
      transferPair.getTo().setInitialCapacity(count);
    }
    bitVector.setValueCount(dataVector.getValueCount());
    // 拷贝过滤后的数据
    int index = 0;
    for (int i = 0; i < bitVector.getValueCount(); i++) {
      Boolean filtered = bitVector.getObject(i);
      if (ObjectUtils.isNotNull(filtered) && bitVector.getObject(i)) {
        appendValue(filteredTransferPair, index, i);;
        index++;
      }
    }
    //回收输入数据内存
    vectorSchemaRoot.close();
    bitVector.close();
    searchVector.close();
    List<FieldVector> resultVector =
            filteredTransferPair.stream().map(x -> (FieldVector) x.getTo()).toList();
    for (FieldVector valueVectors : resultVector) {
      valueVectors.setValueCount(count);
    }
    return new VectorSchemaRoot(resultVector);
  }

  private ValueVector buildSearchVector(ValueVector dataVector, List<Object> search,
                                    MemorySpace memorySpace) {
    ValueVector searchVector =  dataVector.getTransferPair(memorySpace.getAllocator()).getTo();
    searchVector.setInitialCapacity(search.size());
    searchVector.reAlloc();
    if (searchVector instanceof BaseIntVector baseIntVector) {
      for (int i = 0; i < search.size(); i++) {
        baseIntVector.setUnsafeWithPossibleTruncate(i, Long.parseLong(search.get(i).toString()));
      }
    } else if (searchVector instanceof VarCharVector varCharVector) {
      for (int i = 0; i < search.size(); i++) {
        varCharVector.set(i, search.get(i).toString().getBytes(StandardCharsets.UTF_8));
      }
    } else {
      throw new ComputeException("暂时不支持这个类型的谓词计算: %s", dataVector.getClass().getName());
    }
    searchVector.setValueCount(search.size());
    return searchVector;
  }

  private boolean match(VectorValueComparator<ValueVector> comparator,
                        ValueVector searchVector,
                        int dataIndex) {
    for (int i = 0; i < searchVector.getValueCount(); i++) {
      if (comparator.compare(dataIndex, i) == 0) {
        return true;
      }
    }
    return false;
  }

  private void appendValue(List<TransferPair> target, int targetIndex, int sourceIndex) {
    for (TransferPair transferPair : target) {
      transferPair.copyValueSafe(sourceIndex, targetIndex);
    }
  }
}
