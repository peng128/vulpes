package net.peng.vulpes.runtime.physics;

import java.util.ArrayList;
import java.util.List;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.OutputExecutorNode;
import net.peng.vulpes.runtime.struct.Row;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.OutputSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of DataOutputExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
public class DataOutputExecutorNode extends OutputExecutorNode {
  public DataOutputExecutorNode(RowHeader outputRowHeader) {
    super(outputRowHeader);
  }

  @Override
  public Segment<?> execute(Segment<?> segment, MemorySpace memorySpace) {
    if (!(segment instanceof ArrowSegment)) {
      throw new ComputeException("不认识数据类型. %s", segment.getClass().getName());
    }
    List<VectorSchemaRoot> vectorSchemaRoots = ((ArrowSegment) segment).get();
    List<Row> data = new ArrayList<>();
    for (VectorSchemaRoot vectorSchemaRoot : vectorSchemaRoots) {
      final List<FieldVector> fieldVectors = vectorSchemaRoot.getFieldVectors();
      for (int i = 0; i < vectorSchemaRoot.getRowCount(); i++) {
        final int index = i;
        final List<Object> row =
                fieldVectors.stream().map(fieldVector -> fieldVector.getObject(index)).toList();
        data.add(new Row(row));
      }
    }
    return new OutputSegment(data, this.outputRowHeader.getColumns());
  }
}
