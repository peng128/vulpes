package net.peng.vulpes.runtime.lanucher.task;

import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.runtime.struct.data.OutputSegment;

/**
 * Description of TaskRunner.
 * 用来执行物理执行计划或者实际操作的任务执行器.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/28
 */
public interface TaskRunner {

  /**
   * 执行输入的{@link RelalgNode}.
   */
  OutputSegment run(RelalgNode relalgNode, SessionManager sessionManager);
}
