package net.peng.vulpes.runtime.lanucher.task;

import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgSet;
import net.peng.vulpes.runtime.struct.data.OutputSegment;

/**
 * Description of SetTaskRunner.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/28
 */
public class SetTaskRunner implements TaskRunner {

  @Override
  public OutputSegment run(RelalgNode relalgNode, SessionManager sessionManager) {
    if (relalgNode instanceof RelalgSet relalgSet) {
      sessionManager.getConfig().set(relalgSet.getParameter(), relalgSet.getValue());
    }
    throw new ComputeException("无法执行set操作。输入[%s]", relalgNode);
  }
}
