package net.peng.vuples.jdbc.mysql;

import java.nio.ByteBuffer;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vuples.jdbc.mysql.execute.StatementExecutor;

/**
 * Description of DefaultStatementExecutor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
public class TestStatementExecutor implements StatementExecutor {
  @Override
  public byte[] execute(String statement, SessionManager sessionManager) {
    ByteBuffer resultBuffer = ByteBuffer.allocate(4096);
    MysqlSystemProp.response(resultBuffer);
    return resultBuffer.array();
  }
}
