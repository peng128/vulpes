package net.peng.vulpes.runtime.convertor;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import net.peng.vulpes.catalog.manager.CatalogManager;
import net.peng.vulpes.catalog.manager.CatalogManagerFactory;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.Parser;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.runtime.PhysicsNodeTestBase;
import net.peng.vulpes.runtime.ResourceFileUtils;
import net.peng.vulpes.runtime.file.FileReader;
import net.peng.vulpes.runtime.physics.FileScanExecutorNode;
import net.peng.vulpes.runtime.physics.PrintExecutorNode;
import net.peng.vulpes.runtime.physics.SearchExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import org.apache.arrow.dataset.file.FileFormat;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of PhysicsNodeBuilderTest.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class PhysicsNodeBuilderTest extends PhysicsNodeTestBase {

  @Test
  public void builderTest() {
    Config config = buildConfig();
    RelalgNode relalgNode = parse("sql1.sql", config);
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(config);
    List<ExecutorNode> executorNode = physicsNodeBuilder.build(relalgNode);
    Assert.assertEquals(1, executorNode.size());
    Assert.assertEquals(sql1Except(config).toString(), executorNode.get(0).toString());
  }

  @Test
  public void builderJoinTest() {
    Config config = buildConfig();
    RelalgNode relalgNode = parse("sql4.sql", config);
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(config);
    List<ExecutorNode> executorNode = physicsNodeBuilder.build(relalgNode);
    Assert.assertEquals(3, executorNode.size());
    Assert.assertEquals(sql1Except(config).toString(), executorNode.get(0).toString());
  }

  @Test
  public void builderParameterTest() {
    Config config = buildConfig();
    RelalgNode relalgNode = parse("parameter.sql", config);
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(config);
    List<ExecutorNode> executorNode = physicsNodeBuilder.build(relalgNode);
    Assert.assertEquals(1, executorNode.size());
    //TODO: 增加值的判断
  }
}
