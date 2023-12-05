package net.peng.vulpes.runtime.physics;

import java.util.List;
import lombok.Getter;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.exchange.ExchangeService;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.OutputExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.util.TransferPair;

/**
 * Description of DataSenderExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/16
 */
@Getter
public class DataSenderExecutorNode extends OutputExecutorNode {

  private final ExchangeService exchangeService;

  public DataSenderExecutorNode(RowHeader outputRowHeader, ExchangeService exchangeService) {
    super(outputRowHeader);
    this.exchangeService = exchangeService;
  }

  @Override
  public Segment<?> execute(Segment<?> segment, MemorySpace memorySpace) {
    if (!(segment instanceof ArrowSegment)) {
      throw new ComputeException("不认识数据类型. %s", segment.getClass().getName());
    }
    final List<VectorSchemaRoot> newVectorSchemaRoots =
            ((ArrowSegment) segment).get().stream().map(VectorSchemaRoot::getFieldVectors)
                    .map(fieldVectors -> {
                      final List<TransferPair> transferPairs = fieldVectors.stream()
                              .map(fieldVector ->
                                      fieldVector.getTransferPair(memorySpace.getAllocator()))
                              .toList();
                      transferPairs.forEach(TransferPair::transfer);
                      return VectorSchemaRoot.of(transferPairs.stream().map(fieldVector ->
                              (FieldVector) fieldVector.getTo()).toList()
                              .toArray(new FieldVector[0]));
                    }).toList();
    exchangeService.put(new ArrowSegment(newVectorSchemaRoots));
    return null;
  }
}
