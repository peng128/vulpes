package net.peng.vuples.parser;

import java.util.Properties;
import net.peng.vulpes.catalog.manager.CatalogManager;
import net.peng.vulpes.catalog.manager.CatalogManagerFactory;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.Parser;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import org.junit.Assert;
import org.junit.Test;

/**
 * SQL解析的测试.
 */
public class ParseTests {

  @Test
  public void havingTest() {
    baseParseTest("having.sql", "having.ast");
    baseParseWithMetaTest("having.sql", "having.ast");
  }

  @Test
  public void groupByTest() {
    baseParseTest("group_by.sql", "group_by.ast");
    baseParseWithMetaTest("group_by.sql", "group_by.ast");
  }

  @Test
  public void orderLimitByTest() {
    baseParseTest("limit_order.sql", "limit_order.ast");
    baseParseWithMetaTest("limit_order.sql", "limit_order.ast");
  }

  @Test
  public void orderByTest() {
    baseParseTest("order.sql", "order.ast");
    baseParseWithMetaTest("order.sql", "order.ast");
  }

  @Test
  public void singleTableTest() {
    baseParseTest("single.sql", "single.ast");
    baseParseWithMetaTest("single.sql", "single.ast");
  }

  @Test
  public void unionTest() {
    baseParseTest("union.sql", "union.ast");
    baseParseWithMetaTest("union.sql", "union.ast");
  }

  @Test
  public void joinTest() {
    baseParseTest("join.sql", "join.ast");
    baseParseWithMetaTest("join.sql", "join.ast");
  }

  @Test
  public void subQueryTest() {
    baseParseTest("sub_query.sql", "sub_query.ast");
    // TODO:添加报错验证.
  }

  @Test
  public void selectAllTest() {
    baseParseTest("select_all.sql", "select_all_without_meta.ast");
    baseParseWithMetaTest("select_all.sql", "select_all_with_meta.ast");
  }

  @Test
  public void selectParametersTest() {
    baseParseTest("parameter.sql", "parameter.ast");
    baseParseWithMetaTest("parameter.sql", "parameter.ast");
  }

  @Test
  public void setTest() {
    baseParseTest("set.sql", "set.ast");
  }

  private void baseParseTest(String inputSqlFileName, String outputExceptFileName) {
    Properties properties = new Properties();
    properties.put(ConfigItems.SESSION_AUTO_INCREMENT.name(), 0L);
    RelalgNode relalgNode =
        (RelalgNode) Parser.parse(ResourceFileUtils.getText("parse-test/" + inputSqlFileName),
            null, SessionManager.builder().config(new Config(properties))
                .currentCatalog("embedded-catalog").currentSchema("test").build());
    Assert.assertEquals(replaceLastEnter(ResourceFileUtils.getText("parse-test/"
        + outputExceptFileName)), replaceLastEnter(relalgNode.explain()));
  }

  private String replaceLastEnter(String input) {
    String output = input;
    while (output.endsWith("\n")) {
      output = output.substring(0, output.length() - 1);
    }
    return output;
  }

  private void baseParseWithMetaTest(String inputSqlFileName, String outputExceptFileName) {
    Properties properties = new Properties();
    properties.put(ConfigItems.CATALOG_STATIC_CONFIG_FILE_PATH.name(),
        ParseTests.class.getClassLoader().getResource("catalog-embedded.yaml").getFile());
    properties.put(ConfigItems.CATALOG_LOADER_CLASS.name(),
        "net.peng.vulpes.catalog.loader.StaticCatalogLoader");
    properties.put(ConfigItems.SESSION_AUTO_INCREMENT.name(), 0L);
    Config config = new Config(properties);
    CatalogManager catalogManager = CatalogManagerFactory.newInstance(config);
    RelalgNode relalgNode = (RelalgNode) Parser.parse(ResourceFileUtils
            .getText("parse-test/" + inputSqlFileName),
        catalogManager, SessionManager.builder().config(config)
            .currentCatalog("embedded-catalog").currentSchema("test").build());
    Assert.assertEquals(replaceLastEnter(ResourceFileUtils.getText("parse-test/"
        + outputExceptFileName)), replaceLastEnter(relalgNode.explain()));
  }
}
