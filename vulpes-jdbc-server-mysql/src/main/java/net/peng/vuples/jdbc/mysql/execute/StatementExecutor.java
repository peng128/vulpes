package net.peng.vuples.jdbc.mysql.execute;

import net.peng.vulpes.common.session.SessionManager;

/**
 * Description of StatementExecutor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
public interface StatementExecutor {

  /**
   * 执行sql语句.
   */
  byte[] execute(String statement, SessionManager sessionManager);
}
