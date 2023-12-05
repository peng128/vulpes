package net.peng.vulpes.runtime.physics;

import lombok.ToString;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.OutputExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of PrintExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/24
 */
@ToString(callSuper = true)
public class PrintExecutorNode extends OutputExecutorNode {

  public PrintExecutorNode(RowHeader outputRowHeader) {
    super(outputRowHeader);
  }

  @Override
  public Segment<?> execute(Segment<?> segment, MemorySpace memorySpace) {
    if (!(segment instanceof ArrowSegment)) {
      throw new ComputeException("不认识数据类型. %s", segment.getClass().getName());
    }
    System.out.println(((ArrowSegment) segment).get().stream()
            .map(VectorSchemaRoot::contentToTSVString).toList());
    return null;
  }
}
