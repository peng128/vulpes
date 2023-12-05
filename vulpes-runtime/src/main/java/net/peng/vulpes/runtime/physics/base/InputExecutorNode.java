package net.peng.vulpes.runtime.physics.base;

import java.util.List;
import lombok.ToString;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of SourceExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
@ToString
public abstract class InputExecutorNode implements ExecutorNode {
  protected final ExecutorNode next;
  protected final RowHeader outputRowHeader;

  public InputExecutorNode(ExecutorNode next, RowHeader outputRowHeader) {
    this.next = next;
    this.outputRowHeader = outputRowHeader;
  }

  @Override
  public ExecutorNode next() {
    return next;
  }

  public abstract Segment<?> fetchData(MemorySpace memorySpace);
}
