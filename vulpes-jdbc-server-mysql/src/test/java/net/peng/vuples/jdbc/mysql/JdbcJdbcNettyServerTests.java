package net.peng.vuples.jdbc.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.session.SessionManagerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Description of JDBCNettyServerTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
@Slf4j
public class JdbcJdbcNettyServerTests {

  private static final JdbcNettyServer NETTY_SERVER =
          new JdbcNettyServer(new TestStatementExecutor(), new SessionManagerFactory());

  @Test
  public void clientTest() throws SQLException {
    log.info("建立连接");
    Connection conn =
            DriverManager.getConnection("jdbc:mysql://localhost:13000", "default", "");
    Statement statement = conn.createStatement();
    ResultSet rs = statement.executeQuery("show version");
    rs.next();
    System.out.println(rs.getString(1));
    statement.close();
    conn.close();
  }

  /**
   * 启动服务端.
   */
  @BeforeClass
  public static void startJdbcServer() throws InterruptedException {
    Thread server = new Thread(() -> {
      try {
        NETTY_SERVER.start();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
    server.start();
    //TODO: 这里找到为什么要等一会儿才能连接
    while (!NETTY_SERVER.isRunning()) {
      Thread.sleep(1);
    }
    log.info("启动jdbc server");
  }

  /**
   * 关闭服务端.
   */
  @AfterClass
  public static void closeJdbcServer() {
    log.info("关闭jdbc server");
    NETTY_SERVER.close();
  }
}
