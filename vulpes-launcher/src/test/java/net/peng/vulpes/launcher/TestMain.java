package net.peng.vulpes.launcher;

import net.peng.vuples.jdbc.mysql.JdbcNettyServer;
import net.peng.vuples.jdbc.mysql.execute.StatementExecutor;
import org.junit.Test;

/**
 * 测试.
 */
public class TestMain {

  @Test
  public void testMain() throws InterruptedException {
    StatementExecutor executor = new DefaultStatementExecutor();
    JdbcNettyServer jdbcNettyServer = new JdbcNettyServer(executor,
            new TestSessionManagerFactory());
    jdbcNettyServer.start();
  }
}