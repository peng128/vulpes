package net.peng.vulpes.runtime.convertor;

import com.google.common.collect.ImmutableList;
import java.util.Properties;
import net.peng.vulpes.catalog.embedded.EmbeddedTableMeta;
import net.peng.vulpes.catalog.table.FileTableMeta;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.function.aggregate.SumFunction;
import net.peng.vulpes.common.model.DataFormat;
import net.peng.vulpes.common.model.TableIdentifier;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.expression.LiteralExpr;
import net.peng.vulpes.parser.algebraic.function.OperatorSymbol;
import net.peng.vulpes.parser.algebraic.logical.RelalgAggregation;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgProjection;
import net.peng.vulpes.parser.algebraic.logical.RelalgScan;
import net.peng.vulpes.parser.algebraic.logical.RelalgSelection;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.file.FileReader;
import net.peng.vulpes.runtime.physics.AggregateExecutorNode;
import net.peng.vulpes.runtime.physics.FileScanExecutorNode;
import net.peng.vulpes.runtime.physics.ProjectionExecutorNode;
import net.peng.vulpes.runtime.physics.SearchExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import org.apache.arrow.dataset.file.FileFormat;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of ConvertorTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/25
 */
public class ConvertorTests {

  @Test
  public void tableScanToFileScanTest() {
    final String columnName = "col1";
    final FileTableMeta fileTableMeta = new EmbeddedTableMeta(ImmutableList.of(columnName),
            ImmutableList.of(new VarcharType()), ImmutableList.of("1"), DataFormat.CSV);
    final RelalgScan scan = RelalgScan.create(TableIdentifier.create(ImmutableList.of("test",
            "table")), fileTableMeta);
    Assert.assertTrue(TableScanToFileScan.CONVERTOR.isMatch(scan));
    final Config config = new Config(new Properties());
    final ExecutorNode node = TableScanToFileScan.CONVERTOR.convert(scan, config, null);
    final FileReader fileReader = new FileReader(fileTableMeta.getDataFiles(),
            FileFormat.valueOf(fileTableMeta.getDataFormat().name()), config);
    final FileScanExecutorNode exceptNode = new FileScanExecutorNode(null, fileReader,
            new RowHeader(ImmutableList.of(ColumnInfo.builder().name(columnName)
                    .dataType(new VarcharType()).build())));
    Assert.assertEquals(exceptNode.toString(), node.toString());
  }

  private RelalgScan buildScan(RowHeader rowHeader) {
    final FileTableMeta fileTableMeta =
            new EmbeddedTableMeta(rowHeader.getColumns().stream()
                    .map(ColumnInfo::getName).toList(), rowHeader.getColumns().stream()
                    .map(ColumnInfo::getDataType).toList(), ImmutableList.of("1"), DataFormat.CSV);
    return RelalgScan.create(TableIdentifier.create(ImmutableList.of("test", "table")),
            fileTableMeta);
  }

  @Test
  public void selectionToSearchExecutorTest() {
    final String columnName = "col1";
    final String predicateValue = "a";
    final ColumnNameExpr columnNameExpr = ColumnNameExpr.create(IdentifierExpr.create(columnName));
    RowHeader rowHeader = new RowHeader(ImmutableList.of(ColumnInfo.builder().name(columnName)
                    .dataType(new VarcharType()).build()));
    columnNameExpr.fillColumnInfo(rowHeader);
    final FunctionRef functionRef = FunctionRef.create(OperatorSymbol.EQUALS.value,
            ImmutableList.of(columnNameExpr, LiteralExpr.create(predicateValue)),
            SessionManager.DEFAULT_SESSION_MANAGER);
    final RelalgSelection relalgSelection = RelalgSelection.create(functionRef);
    final RelalgNode relalgNode = (RelalgNode) buildScan(rowHeader).merge(relalgSelection);
    Assert.assertTrue(SelectionToSearchExecutor.CONVERTOR.isMatch(relalgNode));
    final Config config = new Config(new Properties());
    final ExecutorNode executorNode = SelectionToSearchExecutor.CONVERTOR.convert(relalgNode,
            config, null);
    final SearchExecutorNode except = new SearchExecutorNode(null, columnName, 0,
            ImmutableList.of(predicateValue), rowHeader, rowHeader);
    Assert.assertEquals(except.toString(), executorNode.toString());
  }

  @Test
  public void projectionToExecutorTest() {
    final String columnName1 = "col1";
    final String columnName2 = "col2";
    final ColumnNameExpr columnNameExpr1 =
            ColumnNameExpr.create(IdentifierExpr.create(columnName1));
    final ColumnNameExpr columnNameExpr2 =
            ColumnNameExpr.create(IdentifierExpr.create(columnName2));
    final RowHeader rowHeader = new RowHeader(ImmutableList.of(
            ColumnInfo.builder().name(columnName1).dataType(new VarcharType()).build(),
            ColumnInfo.builder().name(columnName2).dataType(new VarcharType()).build()));
    columnNameExpr1.fillColumnInfo(rowHeader);
    columnNameExpr2.fillColumnInfo(rowHeader);
    final RelalgProjection relalgProjection =
            RelalgProjection.create(ImmutableList.of(columnNameExpr1, columnNameExpr2));
    final RelalgNode relalgNode = (RelalgNode) buildScan(rowHeader).merge(relalgProjection);
    Assert.assertTrue(ProjectionToExecutorNode.CONVERTOR.isMatch(relalgNode));
    final Config config = new Config(new Properties());
    final ExecutorNode executorNode = ProjectionToExecutorNode.CONVERTOR.convert(relalgNode,
            config, null);
    final ProjectionExecutorNode except = new ProjectionExecutorNode(null,
            ImmutableList.of(columnNameExpr1, columnNameExpr2), rowHeader, rowHeader);
    Assert.assertEquals(except.toString(), executorNode.toString());
  }

  @Test
  public void aggregateToExecutorTest() {
    final String columnName1 = "col1";
    final String columnName2 = "col2";
    final String columnName3 = "col3";
    final ColumnNameExpr columnNameExpr1 =
            ColumnNameExpr.create(IdentifierExpr.create(columnName1));
    final ColumnNameExpr columnNameExpr2 =
            ColumnNameExpr.create(IdentifierExpr.create(columnName2));
    final ColumnNameExpr columnNameExpr3 =
            ColumnNameExpr.create(IdentifierExpr.create(columnName3));
    final RowHeader rowHeader = new RowHeader(ImmutableList.of(
            ColumnInfo.builder().name(columnName1).dataType(new VarcharType()).build(),
            ColumnInfo.builder().name(columnName2).dataType(new VarcharType()).build(),
            ColumnInfo.builder().name(columnName3).dataType(new VarcharType()).build()));
    columnNameExpr1.fillColumnInfo(rowHeader);
    columnNameExpr2.fillColumnInfo(rowHeader);
    columnNameExpr3.fillColumnInfo(rowHeader);
    final FunctionRef functionRef = FunctionRef.create("sum",
            ImmutableList.of(columnNameExpr3), SessionManager.DEFAULT_SESSION_MANAGER);
    final RelalgAggregation relalgAggregation =
            RelalgAggregation.create(ImmutableList.of(functionRef),
                    ImmutableList.of(columnNameExpr1, columnNameExpr2));
    final RelalgNode relalgNode = (RelalgNode) buildScan(rowHeader).merge(relalgAggregation);
    Assert.assertTrue(AggregateToExecutorNode.CONVERTOR.isMatch(relalgNode));
    final Config config = new Config(new Properties());
    final ExecutorNode executorNode = AggregateToExecutorNode.CONVERTOR.convert(relalgNode,
            config, null);
    final AggregateExecutorNode except = new AggregateExecutorNode(null, ImmutableList.of(0, 1),
            ImmutableList.of(new SumFunction(ImmutableList.of(2), null)), rowHeader, rowHeader);
    Assert.assertEquals(except.toString(), executorNode.toString());
  }
}
