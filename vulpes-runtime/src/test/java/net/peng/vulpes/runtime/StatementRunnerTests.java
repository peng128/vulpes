package net.peng.vulpes.runtime;

import java.util.List;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.runtime.lanucher.StatementRunner;
import net.peng.vulpes.runtime.struct.Row;
import net.peng.vulpes.runtime.struct.data.OutputSegment;
import org.junit.Assert;
import org.junit.Test;

/**
 * sql执行器测试类.
 */
public class StatementRunnerTests extends PhysicsNodeTestBase {
  StatementRunner statementRunner = new StatementRunner();

  @Test
  public void showCatalogTests() {
    final OutputSegment outputSegment = statementRunner
        .run(ResourceFileUtils.getText("sql/show_catalog.sql"),
            SessionManager.builder().config(buildConfig()).build());
    Assert.assertEquals(
        buildMetaOutputSegment("CATALOG", List.of("embedded-catalog")).toString(),
        outputSegment.toString());
    System.out.println(outputSegment);
  }

  @Test
  public void showSchemaTests() {
    final OutputSegment outputSegment = statementRunner
        .run(ResourceFileUtils.getText("sql/show_schema.sql"),
            SessionManager.builder().currentCatalog("embedded-catalog").currentSchema("test")
                .config(buildConfig()).build());
    Assert.assertEquals(
        buildMetaOutputSegment("SCHEMA", List.of("test")).toString(),
        outputSegment.toString());
    System.out.println(outputSegment);
  }

  @Test
  public void showTableTests() {
    final OutputSegment outputSegment = statementRunner
        .run(ResourceFileUtils.getText("sql/show_table.sql"),
            SessionManager.builder().currentCatalog("embedded-catalog").currentSchema("test")
                .config(buildConfig()).build());
    Assert.assertEquals(
        buildMetaOutputSegment("TABLE",
            List.of("table1", "table3", "table1-2", "table2", "table4")).toString(),
        outputSegment.toString());
    System.out.println(outputSegment);
  }

  private OutputSegment buildMetaOutputSegment(String columnName, List<String> data) {
    final List<ColumnInfo> columns =
        List.of(ColumnInfo.builder().name(columnName).dataType(new VarcharType()).build());
    final List<Row> rows = data.stream().map(ele -> new Row(List.of(ele))).toList();
    return new OutputSegment(rows, columns);
  }
}
