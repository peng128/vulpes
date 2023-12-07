package net.peng.vuples.parser;

import java.util.Properties;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.Parser;
import net.peng.vulpes.parser.algebraic.meta.RelalgMetaNode;
import org.junit.Test;

/**
 * 元数据请求测试类.
 */
public class MetaParseTests {

  @Test
  public void showCatalogTest() {
    baseParseTest("show_catalog.sql");
  }

  private void baseParseTest(String inputSqlFileName) {
    Properties properties = new Properties();
    properties.put(ConfigItems.SESSION_AUTO_INCREMENT.name(), 0L);
    RelalgMetaNode relalgNode =
        (RelalgMetaNode) Parser.parse(ResourceFileUtils.getText("meta-parse/" + inputSqlFileName),
            null, SessionManager.builder().config(new Config(properties))
                .currentCatalog("embedded-catalog").currentSchema("test").build());
  }
}
