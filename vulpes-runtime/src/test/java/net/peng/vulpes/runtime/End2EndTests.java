package net.peng.vulpes.runtime;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.logical.InputRelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.SingleInputRelalgNode;
import net.peng.vulpes.runtime.convertor.PhysicsNodeBuilder;
import net.peng.vulpes.runtime.framework.local.LocalPipelineChain;
import net.peng.vulpes.runtime.lanucher.StatementRunner;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.OutputSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of End2EndTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
@Slf4j
public class End2EndTests extends PhysicsNodeTestBase {

  @Test
  public void test2() {
    simpleSql1Test();
    simpleSql2Test();
    simpleSql1Test();
  }

  @Test
  public void simpleSql1Test() {
    long start = System.currentTimeMillis();
    Config config = buildConfig();
    System.out.println("env " + (System.currentTimeMillis() - start) + " ms");
    RelalgNode relalgNode = (RelalgNode) parse("sql1.sql", config);
    System.out.println("parse " + (System.currentTimeMillis() - start) + " ms");
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(config);
    List<ExecutorNode> executorNodes = physicsNodeBuilder.build(relalgNode);
    System.out.println("physics convert " + (System.currentTimeMillis() - start) + " ms");
    Assert.assertEquals(1, executorNodes.size());
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    System.out.println("run start " + (System.currentTimeMillis() - start) + " ms");
    LocalPipelineChain localPipelineChain = new LocalPipelineChain(executorNodes.get(0),
            memorySpace, config);
    Segment<?> result = localPipelineChain.execute();
    allocator.close();
    System.out.println(result);
    System.out.println("run end " + (System.currentTimeMillis() - start) + " ms");
  }

  @Test
  public void simpleSql2Test() {
    long start = System.currentTimeMillis();
    Config config = buildConfig();
    System.out.println("env " + (System.currentTimeMillis() - start) + " ms");
    RelalgNode relalgNode = (RelalgNode) parse("sql2.sql", config);
    System.out.println("parse " + (System.currentTimeMillis() - start) + " ms");
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(config);
    List<ExecutorNode> executorNodes = physicsNodeBuilder.build(relalgNode);
    System.out.println("physics convert " + (System.currentTimeMillis() - start) + " ms");
    Assert.assertEquals(1, executorNodes.size());
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    System.out.println("run start " + (System.currentTimeMillis() - start) + " ms");
    LocalPipelineChain localPipelineChain = new LocalPipelineChain(executorNodes.get(0),
            memorySpace, config);
    localPipelineChain.execute();
    allocator.close();
    System.out.println("run end " + (System.currentTimeMillis() - start) + " ms");
  }

  @Test
  public void simpleSql3Test() {
    long start = System.currentTimeMillis();
    Config config = buildConfig();
    System.out.println("env " + (System.currentTimeMillis() - start) + " ms");
    RelalgNode relalgNode = (RelalgNode) parse("sql3.sql", config);
    System.out.println("parse " + (System.currentTimeMillis() - start) + " ms");
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(config);
    List<ExecutorNode> executorNodes = physicsNodeBuilder.build(relalgNode);
    System.out.println("physics convert " + (System.currentTimeMillis() - start) + " ms");
    Assert.assertEquals(1, executorNodes.size());
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    System.out.println("run start " + (System.currentTimeMillis() - start) + " ms");
    LocalPipelineChain localPipelineChain = new LocalPipelineChain(executorNodes.get(0),
            memorySpace, config);
    localPipelineChain.execute();
    allocator.close();
    System.out.println("run end " + (System.currentTimeMillis() - start) + " ms");
  }

  @Test
  public void joinSql4Test() {
    long start = System.currentTimeMillis();
    Config config = buildConfig();
    System.out.println("env " + (System.currentTimeMillis() - start) + " ms");
    RelalgNode relalgNode = (RelalgNode) parse("sql4.sql", config);
    System.out.println("parse " + (System.currentTimeMillis() - start) + " ms");
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(config);
    List<ExecutorNode> executorNodes = physicsNodeBuilder.build(relalgNode);
    System.out.println("physics convert " + (System.currentTimeMillis() - start) + " ms");
    Assert.assertEquals(3, executorNodes.size());
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    System.out.println("run start " + (System.currentTimeMillis() - start) + " ms");
    Segment<?> outputSegment = null;
    for (int i = 0; i < executorNodes.size(); i++) {
      LocalPipelineChain localPipelineChain = new LocalPipelineChain(executorNodes.get(i),
              memorySpace, config);
      outputSegment = localPipelineChain.execute();
    }
    allocator.close();
    log.debug(((OutputSegment) outputSegment).get().toString());
    System.out.println("run end " + (System.currentTimeMillis() - start) + " ms");
  }

  @Test
  public void parameterSqlTest() {
    long start = System.currentTimeMillis();
    Config config = buildConfig();
    System.out.println("env " + (System.currentTimeMillis() - start) + " ms");
    RelalgNode relalgNode = (RelalgNode) parse("parameter.sql", config);
    if (ObjectUtils.isEmpty(relalgNode.getRowHeader())
            && relalgNode instanceof SingleInputRelalgNode singleInputRelalgNode) {
      singleInputRelalgNode.setRowHeader(singleInputRelalgNode.computeOutputHeader(null));
    }
    System.out.println("parse " + (System.currentTimeMillis() - start) + " ms");
    PhysicsNodeBuilder physicsNodeBuilder = new PhysicsNodeBuilder(config);
    List<ExecutorNode> executorNodes = physicsNodeBuilder.build(relalgNode);
    System.out.println("physics convert " + (System.currentTimeMillis() - start) + " ms");
    Assert.assertEquals(1, executorNodes.size());
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    System.out.println("run start " + (System.currentTimeMillis() - start) + " ms");
    Segment<?> outputSegment = null;
    for (ExecutorNode executorNode : executorNodes) {
      LocalPipelineChain localPipelineChain = new LocalPipelineChain(executorNode,
              memorySpace, config);
      outputSegment = localPipelineChain.execute();
    }
    if (outputSegment instanceof OutputSegment output) {
      log.debug("输出为: {}", output.get());
    }
    allocator.close();
    System.out.println("run end " + (System.currentTimeMillis() - start) + " ms");
  }

  @Test
  public void sql5Test() {
    Config config = buildConfig();
    StatementRunner statementRunner = new StatementRunner();
    final OutputSegment outputSegment = statementRunner
        .run(ResourceFileUtils.getText("sql/sql5.sql"),
        SessionManager.builder().config(config).currentCatalog("embedded-catalog")
            .currentSchema("test").build());
    System.out.println(outputSegment);
  }

  @Test
  public void sql6Test() {
    Config config = buildConfig();
    StatementRunner statementRunner = new StatementRunner();
    final OutputSegment outputSegment = statementRunner
        .run(ResourceFileUtils.getText("sql/sql6" + ".sql"),
        SessionManager.builder().config(config).currentCatalog("embedded-catalog")
            .currentSchema("test").build());
    System.out.println(outputSegment);
  }

  @Test
  public void tpchQuery01() {
    final OutputSegment outputSegment = tpchQuery("q01");
    System.out.println(outputSegment);
  }

  private OutputSegment tpchQuery(String queryName) {
    Config config = buildConfig();
    StatementRunner statementRunner = new StatementRunner();
    return statementRunner.run(getTpchSql(queryName),
            SessionManager.builder().config(config).currentCatalog("embedded-catalog")
                .currentSchema("test").build());
  }
}
