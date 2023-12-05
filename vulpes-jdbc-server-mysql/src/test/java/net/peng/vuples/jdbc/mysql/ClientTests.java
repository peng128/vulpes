package net.peng.vuples.jdbc.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Test;

/**
 * Description of ClientTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/16
 */
public class ClientTests {

  @Test
  public void testRequest() throws SQLException {
    Connection conn =
            DriverManager.getConnection("jdbc:mysql://localhost:13000", "root", "abc");
    Statement statement = conn.createStatement();
    ResultSet rs = statement.executeQuery("select \"name\", age, gender, \"type\" from "
            + "table1 t1 join test.table2 t2 on t1.id = t2.id where t1.id = 3");
    while (rs.next()) {
      System.out.println(rs.getString(1));
    }
    statement.close();
    conn.close();
  }
}
