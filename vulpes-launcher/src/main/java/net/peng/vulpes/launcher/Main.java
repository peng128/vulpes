package net.peng.vulpes.launcher;

import net.peng.vulpes.common.session.ConfigurationLoaderSessionManagerFactory;
import net.peng.vuples.jdbc.mysql.JdbcNettyServer;
import net.peng.vuples.jdbc.mysql.execute.StatementExecutor;

/**
 * 主方法.
 */
public class Main {

  /**
   * 启动.
   */
  public static void main(String[] args) throws InterruptedException {
    StatementExecutor executor = new DefaultStatementExecutor();
    JdbcNettyServer jdbcNettyServer = new JdbcNettyServer(executor,
            new ConfigurationLoaderSessionManagerFactory());
    jdbcNettyServer.start();
  }
}