package net.peng.vulpes.runtime.lanucher.task;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.SingleInputRelalgNode;
import net.peng.vulpes.runtime.convertor.PhysicsNodeBuilder;
import net.peng.vulpes.runtime.framework.local.LocalPipelineChain;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.OutputSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;

/**
 * Description of QueryTaskRunner.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/28
 */
@Slf4j
public class QueryTaskRunner implements TaskRunner {
  @Override
  public OutputSegment run(RelationAlgebraic relationAlgebraic, SessionManager sessionManager) {
    if (!(relationAlgebraic instanceof RelalgNode relalgNode)) {
      throw new ComputeException("不能查询执行请求:%s", relationAlgebraic);
    }
    Stopwatch stopwatch = Stopwatch.createStarted();
    fullEmptyMeta(relalgNode);
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(sessionManager.getConfig());
    final List<ExecutorNode> executorNodes = physicsNodeBuilder.build(relalgNode);
    log.debug("[{}] physics convert {} ms", sessionManager.getSessionId(),
            stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    stopwatch.reset().start();
    OutputSegment outputSegment = executorRun(executorNodes, sessionManager);
    log.debug("[{}] executor runner {} ms", sessionManager.getSessionId(),
            stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    return outputSegment;
  }

  /**
   * 补充{@link RelalgNode}中缺失的元数据信息.
   */
  private void fullEmptyMeta(RelalgNode relalgNode) {
    if (ObjectUtils.isEmpty(relalgNode.getRowHeader())
            && relalgNode instanceof SingleInputRelalgNode singleInputRelalgNode) {
      // 补充只有select 而没有from 表的输出结果.
      singleInputRelalgNode.setRowHeader(singleInputRelalgNode.computeOutputHeader(null));
    }
  }

  /**
   * 执行物理执行节点.
   */
  private OutputSegment executorRun(List<ExecutorNode> executorNodes,
                                    SessionManager sessionManager) {
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    Segment<?> result = null;
    for (int i = 0; i < executorNodes.size(); i++) {
      LocalPipelineChain localPipelineChain = new LocalPipelineChain(executorNodes.get(i),
              memorySpace, sessionManager.getConfig());
      result = localPipelineChain.execute();
    }
    allocator.close();
    if (ObjectUtils.isNull(result)) {
      return null;
    }
    if (!(result instanceof OutputSegment)) {
      throw new ComputeException("输出需要是 OutputSegment, 但是{}", result.getClass().getName());
    }
    return (OutputSegment) result;
  }
}
