package net.peng.vulpes.runtime.lanucher;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.catalog.manager.CatalogManager;
import net.peng.vulpes.catalog.manager.CatalogManagerFactory;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.Parser;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgSet;
import net.peng.vulpes.parser.algebraic.logical.SingleInputRelalgNode;
import net.peng.vulpes.runtime.convertor.PhysicsNodeBuilder;
import net.peng.vulpes.runtime.framework.local.LocalPipelineChain;
import net.peng.vulpes.runtime.lanucher.task.QueryTaskRunner;
import net.peng.vulpes.runtime.lanucher.task.SetTaskRunner;
import net.peng.vulpes.runtime.lanucher.task.TaskRunner;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.OutputSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;

/**
 * Description of StatementRunner.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
@Slf4j
public class StatementRunner {

  /**
   * 执行语句.
   */
  public OutputSegment run(String statement, SessionManager sessionManager) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    CatalogManager catalogManager = CatalogManagerFactory.newInstance(sessionManager.getConfig());
    final RelalgNode relalgNode = Parser.parse(statement, catalogManager, sessionManager);
    log.debug("[{}] parse {} ms", sessionManager.getSessionId(),
            stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    stopwatch.reset().start();
    TaskRunner taskRunner;
    if (relalgNode instanceof RelalgSet) {
      taskRunner = new SetTaskRunner();
    } else {
      taskRunner = new QueryTaskRunner();
    }
    return taskRunner.run(relalgNode, sessionManager);
  }
}
