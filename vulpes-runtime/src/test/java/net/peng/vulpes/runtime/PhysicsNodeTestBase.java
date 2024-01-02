package net.peng.vulpes.runtime;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.embedded.EmbeddedCatalog;
import net.peng.vulpes.catalog.manager.CatalogManager;
import net.peng.vulpes.catalog.manager.CatalogManagerFactory;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.model.TableIdentifier;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.Parser;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.convertor.PhysicsNodeBuilderTest;
import net.peng.vulpes.runtime.file.FileReader;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.FileScanExecutorNode;
import net.peng.vulpes.runtime.physics.PrintExecutorNode;
import net.peng.vulpes.runtime.physics.SearchExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import org.apache.arrow.dataset.file.FileFormat;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.junit.Assert;

/**
 * Description of PhysicsNodeTestBase.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class PhysicsNodeTestBase {
  protected final String catalogTestFileBase = this.getClass().getClassLoader()
          .getResource("embedded-catalog").getPath();

  protected RelationAlgebraic parse(String inputSqlFileName, Config config) {
    CatalogManager catalogManager = CatalogManagerFactory.newInstance(config);
    return Parser.parse(ResourceFileUtils.getText("sql/" + inputSqlFileName), catalogManager,
            SessionManager.builder().config(config)
                    .currentCatalog("embedded-catalog").currentSchema("test").build());
  }

  protected String getTpchSql(String queryName) {
    String statement = ResourceFileUtils.getText("sql/tpch/" + queryName + ".sql");
    return statement.replaceAll("\\$\\{database}", "arrow")
        .replaceAll("\\$\\{schema}", "tpch")
        .replaceAll("\\$\\{prefix}", "");
  }

  protected Config buildConfig() {
    Properties properties = new Properties();
    properties.put(ConfigItems.CATALOG_STATIC_CONFIG_FILE_PATH.name(),
            PhysicsNodeBuilderTest.class.getClassLoader().getResource("catalog-embedded.yaml")
                    .getFile());
    properties.put(ConfigItems.CATALOG_LOADER_CLASS.name(),
            "net.peng.vulpes.catalog.loader.StaticCatalogLoader");
    Function<String, String> path = input -> "file://" + PhysicsNodeBuilderTest.class
            .getClassLoader().getResource(input).getFile();
    properties.put(ConfigItems.FILE_PATH_PROCESS_WRAPPER.name(), path);
    return new Config(properties);
  }

  protected ExecutorNode sql1Except(Config config) {
    RowHeader tableRowHeader = getTableRowHeader();
    // 输出节点
    PrintExecutorNode printExecutorNode = new PrintExecutorNode(tableRowHeader);
    // 过滤节点
    SearchExecutorNode searchExecutorNode = new SearchExecutorNode(printExecutorNode, "age", 2,
            ImmutableList.of(24), tableRowHeader, tableRowHeader);
    // 扫描节点
    FileReader fileReader =  genFileReader("data/table1.csv", FileFormat.CSV, config);
    FileScanExecutorNode fileScanExecutorNode = new FileScanExecutorNode(searchExecutorNode,
            fileReader, tableRowHeader);
    return fileScanExecutorNode;
  }

  protected FileReader genFileReader(String fileName, FileFormat fileFormat, Config config) {
    return new FileReader(ImmutableList.of("file://" + this.getClass().getClassLoader()
            .getResource(fileName).getFile()), fileFormat, config);
  }

  protected List<VectorSchemaRoot> readData(String fileName, FileFormat fileFormat,
                                    MemorySpace memorySpace) {
    Properties properties = new Properties();
    //properties.put(ConfigItems.FILE_READ_ROW_BATCH_SIZE.name(), 3L);
    FileReader fileReader = new FileReader(ImmutableList.of("file://" + this.getClass()
            .getClassLoader().getResource(fileName).getFile()), fileFormat,
            new Config(properties));
    return ((ArrowSegment) fileReader.fetch(memorySpace)).get();
  }

  protected RowHeader getTableRowHeader() {
    return new RowHeader(ImmutableList.of(
                    ColumnInfo.builder().name("id").dataType(new BigIntType()).build(),
                    ColumnInfo.builder().name("name").dataType(new VarcharType()).build(),
                    ColumnInfo.builder().name("age").dataType(new BigIntType()).build(),
                    ColumnInfo.builder().name("gender").dataType(new VarcharType()).build(),
                    ColumnInfo.builder().name("phone").dataType(new BigIntType()).build()));
  }

  /**
   * 根据表结构配置构建{@link RowHeader}.
   */
  protected RowHeader buildTableRowHeader(String alias, String database, String table) {
    Catalog catalog = new EmbeddedCatalog(catalogTestFileBase);
    TableMeta tableMeta = catalog.getTable(TableIdentifier.create(
            Arrays.asList("embedded-catalog", database, table)));
    return new RowHeader(new RowHeader(tableMeta), Optional.of(alias));
  }
}
