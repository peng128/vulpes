package net.peng.vulpes.runtime.framework;

import jdk.jfr.Experimental;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.Segment;

/**
 * Description of Runner.
 * TODO 希望将{@link ExecutorNode}中具体执行的代码迁移到这个接口中.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/10
 */
@Experimental
public abstract class TaskRunner<T extends Segment<?>, R extends Segment<?>,
        V extends ExecutorNode> {

  protected final V executorNode;

  public TaskRunner(V executorNode) {
    this.executorNode = executorNode;
  }

  /**
   * 执行任务.
   */
  public abstract R execute(T input, MemorySpace memorySpace);
}
