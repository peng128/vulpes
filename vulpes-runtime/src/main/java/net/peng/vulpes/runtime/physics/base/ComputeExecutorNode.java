package net.peng.vulpes.runtime.physics.base;

import java.util.List;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of ScalarExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
@ToString
public abstract class ComputeExecutorNode implements ExecutorNode {
  protected final ExecutorNode next;
  @Getter
  protected final RowHeader inputRowHeader;
  @Getter
  protected final RowHeader outputRowHeader;

  /**
   * 初始化.
   */
  public ComputeExecutorNode(ExecutorNode next, RowHeader inputRowHeader,
                             RowHeader outputRowHeader) {
    this.next = next;
    this.inputRowHeader = inputRowHeader;
    this.outputRowHeader = outputRowHeader;
  }

  @Override
  public ExecutorNode next() {
    return next;
  }

  /**
   * 单输入的执行方法.
   */
  public abstract Segment<?> executeSingleInput(
          Segment<?> segment, MemorySpace memorySpace);
}
