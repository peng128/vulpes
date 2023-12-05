package net.peng.vulpes.runtime.function.execute.arrow;

import java.util.List;
import lombok.ToString;
import net.peng.vulpes.runtime.function.execute.ExpressionExecutor;
import net.peng.vulpes.runtime.memory.MemorySpace;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.util.TransferPair;

/**
 * Description of ColumnProjection.
 * 字段投影.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/28
 */
@ToString
public class ArrowColumnExpression
        implements ExpressionExecutor<List<FieldVector>, FieldVector> {

  private final int columnIndex;

  public ArrowColumnExpression(int columnIndex) {
    this.columnIndex = columnIndex;
  }

  @Override
  public FieldVector execute(List<FieldVector> valueVectorList, MemorySpace memorySpace) {
    TransferPair transferPair =
            valueVectorList.get(columnIndex).getTransferPair(memorySpace.getAllocator());
    transferPair.transfer();
    return (FieldVector) transferPair.getTo();
  }
}
