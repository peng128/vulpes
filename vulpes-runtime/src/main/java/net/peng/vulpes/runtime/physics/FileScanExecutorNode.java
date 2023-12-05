package net.peng.vulpes.runtime.physics;

import lombok.ToString;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.file.FileReader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.physics.base.InputExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;

/**
 * Description of FileScanExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
@ToString(callSuper = true)
public class FileScanExecutorNode extends InputExecutorNode {
  private final FileReader fileReader;

  public FileScanExecutorNode(ExecutorNode next, FileReader fileReader,
                              RowHeader outputRowHeader) {
    super(next, outputRowHeader);
    this.fileReader = fileReader;
  }

  @Override
  public Segment<?> fetchData(MemorySpace memorySpace) {
    Segment<?> segment = fileReader.fetch(memorySpace);
    if (!(segment instanceof ArrowSegment)) {
      throw new ComputeException("不认识的内存数据结构[%s].", segment.getClass().getName());
    }
    return segment;
  }
}
