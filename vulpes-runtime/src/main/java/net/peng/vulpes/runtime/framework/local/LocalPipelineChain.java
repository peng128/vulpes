package net.peng.vulpes.runtime.framework.local;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.runtime.framework.PipelineChain;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ComputeExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.physics.base.InputExecutorNode;
import net.peng.vulpes.runtime.physics.base.OutputExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of LocalPipelineChain.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/20
 */
@Slf4j
public class LocalPipelineChain implements PipelineChain {

  private final ExecutorNode executorNode;

  private final MemorySpace memorySpace;

  private final Config config;

  /**
   * 本地执行流水线.
   */
  public LocalPipelineChain(ExecutorNode executorNode, MemorySpace memorySpace,
                            Config config) {
    this.executorNode = executorNode;
    this.memorySpace = memorySpace;
    this.config = config;
  }

  @Override
  public Segment<?> execute() {
    Segment<?> data = null;
    ExecutorNode currentNode = new DummyExecutorNode(executorNode);
    while (currentNode.hashNext()) {
      currentNode = currentNode.next();
      long start = System.currentTimeMillis();
      data = internalExecute(currentNode, data);
      log.debug("pipeline element run [{}] cost: {} ms",
              currentNode.getClass().getName(), System.currentTimeMillis() - start);
    }
    return data;
  }

  private Segment<?> internalExecute(ExecutorNode executorNode,
                                                 Segment<?> data) {
    if (executorNode instanceof InputExecutorNode) {
      return ((InputExecutorNode) executorNode).fetchData(memorySpace);
    } else if (executorNode instanceof OutputExecutorNode) {
      Segment<?> result = ((OutputExecutorNode) executorNode).execute(data, memorySpace);
      clear(data);
      return result;
    } else {
      Segment<?> result = ((ComputeExecutorNode) executorNode).executeSingleInput(data,
              memorySpace);
      clear(data);
      return result;
    }
  }

  private void clear(Segment<?> data) {
    if (ObjectUtils.isNotNull(data)) {
      if (data instanceof ArrowSegment arrowSegment) {
        try {
          AutoCloseables.close(arrowSegment.get());
        } catch (Exception e) {
          throw new ComputeException("中间结果数据无法关闭.", e);
        }
      }
    }
  }

  private static class DummyExecutorNode implements ExecutorNode {

    private final ExecutorNode nextNode;

    public DummyExecutorNode(ExecutorNode nextNode) {
      this.nextNode = nextNode;
    }

    @Override
    public ExecutorNode next() {
      return nextNode;
    }

    @Override
    public boolean hashNext() {
      return true;
    }
  }
}
