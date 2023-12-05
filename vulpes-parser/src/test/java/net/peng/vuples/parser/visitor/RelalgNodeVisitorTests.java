package net.peng.vuples.parser.visitor;

import java.util.Properties;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.Parser;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vuples.parser.ResourceFileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of RelalgNodeVisitorTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class RelalgNodeVisitorTests {

  @Test
  public void scanCounterTest() {
    final SimpleRelalgNodeVisitor simpleRelalgNodeVisitor = new SimpleRelalgNodeVisitor();
    final RelalgNode relalgNode = parse("join.sql");
    final RelalgNode result = relalgNode.accept(simpleRelalgNodeVisitor);
    Assert.assertEquals(3, simpleRelalgNodeVisitor.counter);
    Assert.assertEquals(result, relalgNode);
  }

  private RelalgNode parse(String inputSqlFileName) {
    return Parser.parse(ResourceFileUtils.getText("parse-test/" + inputSqlFileName), null,
                    SessionManager.builder().config(new Config(new Properties()))
                            .currentCatalog("embedded-catalog").currentSchema("test").build());
  }
}
