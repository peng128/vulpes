package net.peng.vulpes.runtime.physics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.function.execute.arrow.ArrowExecuteTools;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ComputeExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.vector.BitVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.util.TransferPair;

/**
 * Description of FilterExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/30
 */
@Slf4j
@ToString(callSuper = true)
public class SelectionExecutorNode extends ComputeExecutorNode {

  private final RelalgExpr predicates;

  /**
   * 过滤执行节点.
   */
  public SelectionExecutorNode(ExecutorNode next, RelalgExpr predicates, RowHeader inputRowHeader,
                               RowHeader outputRowHeader) {
    super(next, inputRowHeader, outputRowHeader);
    this.predicates = predicates;
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
    final List<FieldVector> fieldVectors = vectorSchemaRoot.getFieldVectors();
    final List<TransferPair> filteredTransferPair = fieldVectors.stream()
            .map(valueVector -> valueVector.getTransferPair(memorySpace.getAllocator()))
            .collect(Collectors.toList());

    //标记过滤
    FieldVector fieldVector = ArrowExecuteTools.computeExpr(predicates, fieldVectors,
            memorySpace, inputRowHeader);
    int rowCount = 0;
    if (fieldVector instanceof BitVector bitVector) {
      for (int i = 0; i < bitVector.getValueCount(); i++) {
        Boolean filtered = bitVector.getObject(i);
        if (ObjectUtils.isNotNull(filtered) && bitVector.getObject(i)) {
          rowCount++;
        }
      }
    }
    for (TransferPair transferPair : filteredTransferPair) {
      transferPair.getTo().setInitialCapacity(rowCount);
      transferPair.getTo().reAlloc();
    }
    // 拷贝过滤后的数据

    if (fieldVector instanceof BitVector bitVector) {
      int index = 0;
      for (int i = 0; i < bitVector.getValueCount(); i++) {
        Boolean filtered = bitVector.getObject(i);
        if (ObjectUtils.isNotNull(filtered) && bitVector.getObject(i)) {
          appendValue(filteredTransferPair, index, i);;
          index++;
        }
      }
    }
    List<FieldVector> resultVector =
            filteredTransferPair.stream().map(x -> (FieldVector) x.getTo()).toList();
    for (FieldVector valueVectors : resultVector) {
      valueVectors.setValueCount(rowCount);
    }
    //回收输入数据内存
    vectorSchemaRoot.close();
    fieldVector.close();
    return new VectorSchemaRoot(resultVector);
  }

  private void appendValue(List<TransferPair> target, int targetIndex, int sourceIndex) {
    for (TransferPair transferPair : target) {
      transferPair.copyValueSafe(sourceIndex, targetIndex);
    }
  }
}
