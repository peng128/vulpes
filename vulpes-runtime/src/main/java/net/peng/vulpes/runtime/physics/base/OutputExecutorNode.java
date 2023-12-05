package net.peng.vulpes.runtime.physics.base;

import lombok.ToString;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.Segment;

/**
 * Description of SinkExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
@ToString
public abstract class OutputExecutorNode implements ExecutorNode {
  protected final RowHeader outputRowHeader;

  protected OutputExecutorNode(RowHeader outputRowHeader) {
    this.outputRowHeader = outputRowHeader;
  }

  // 输出节点，没有下一个的概念.
  @Override
  public ExecutorNode next() {
    return null;
  }

  public abstract Segment<?> execute(Segment<?> segment,
                                  MemorySpace memorySpace);
}
