package net.peng.vulpes.runtime.physics;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.logical.RelalgJoin;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.PhysicsNodeTestBase;
import net.peng.vulpes.runtime.exchange.MemoryDataFetcher;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import org.apache.arrow.dataset.file.FileFormat;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of JoinExecutorNodeTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/15
 */
public class JoinExecutorNodeTests extends PhysicsNodeTestBase {

  @Test
  public void singleMatchTest() throws Exception {
    simpleJoinTest(RelalgJoin.JoinType.INNER_JOIN, RelalgJoin.JoinSide.FULL, "table1", "table2",
            """
                    [id\t name\t age\t gender\t phone\tid\t type
                    1\t joe\t24\t male\t151\t1\t t1
                    5\t bob\t41\t female\t135\t5\t t2
                    4\t ham\t24\t male\t155\t4\t t1
                    , id\t name\t age\t gender\t phone\tid\t type
                    2\t anny\t30\t female\t1312\t2\t t3
                    3\t cloud\t18\t male\t1513\t3\t t1
                    ]""");
  }

  @Test
  public void rightMissInnerMatchTest() throws Exception {
    simpleJoinTest(RelalgJoin.JoinType.INNER_JOIN, RelalgJoin.JoinSide.FULL, "table1", "table3",
            """
                    [id\t name\t age\t gender\t phone\tid\t type
                    1\t joe\t24\t male\t151\t1\t t1
                    4\t ham\t24\t male\t155\t4\t t1
                    , id\t name\t age\t gender\t phone\tid\t type
                    2\t anny\t30\t female\t1312\t2\t t3
                    3\t cloud\t18\t male\t1513\t3\t t1
                    ]""");
  }

  @Test
  public void rightMissLeftMatchTest() throws Exception {
    simpleJoinTest(RelalgJoin.JoinType.OUTER_JOIN, RelalgJoin.JoinSide.LEFT, "table1", "table3",
            """
                    [id\t name\t age\t gender\t phone\tid\t type
                    1\t joe\t24\t male\t151\t1\t t1
                    5\t bob\t41\t female\t135\tnull\tnull
                    4\t ham\t24\t male\t155\t4\t t1
                    , id\t name\t age\t gender\t phone\tid\t type
                    2\t anny\t30\t female\t1312\t2\t t3
                    3\t cloud\t18\t male\t1513\t3\t t1
                    ]""");
  }

  @Test
  public void rightDuplicationMatchTest() throws Exception {
    simpleJoinTest(RelalgJoin.JoinType.OUTER_JOIN, RelalgJoin.JoinSide.LEFT, "table1", "table4",
            """
                    [id\t name\t age\t gender\t phone\tid\t type
                    1\t joe\t24\t male\t151\t1\t t1
                    5\t bob\t41\t female\t135\t5\t t2
                    4\t ham\t24\t male\t155\t4\t t3
                    4\t ham\t24\t male\t155\t4\t t1
                    , id\t name\t age\t gender\t phone\tid\t type
                    2\t anny\t30\t female\t1312\t2\t t3
                    3\t cloud\t18\t male\t1513\t3\t t1
                    ]""");
  }

  @Test
  public void leftDuplicationMatchTest() throws Exception {
    simpleJoinTest(RelalgJoin.JoinType.OUTER_JOIN, RelalgJoin.JoinSide.LEFT, "table1-2", "table2",
            """
                    [id\t name\t age\t gender\t phone\tid\t type
                    5\t bob\t41\t female\t135\t5\t t2
                    1\t joe\t24\t male\t151\t1\t t1
                    5\t bob\t41\t female\t135\t5\t t2
                    , id\t name\t age\t gender\t phone\tid\t type
                    4\t ham\t24\t male\t155\t4\t t1
                    2\t anny\t30\t female\t1312\t2\t t3
                    3\t cloud\t18\t male\t1513\t3\t t1
                    , id\t name\t age\t gender\t phone\tid\t type
                    5\t bob\t41\t female\t135\t5\t t2
                    ]""");
  }

  private void simpleJoinTest(RelalgJoin.JoinType joinType, RelalgJoin.JoinSide joinSide,
                              String table1, String table2, String except) throws Exception {
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    // 准备数据
    List<VectorSchemaRoot> data1 = readData("data/" + table1 + ".csv", FileFormat.CSV, memorySpace);
    List<VectorSchemaRoot> data2 = readData("data/" + table2 + ".csv", FileFormat.CSV, memorySpace);
    MemoryDataFetcher memoryDataFetcher1 = new MemoryDataFetcher(new ArrowSegment(data1));
    MemoryDataFetcher memoryDataFetcher2 = new MemoryDataFetcher(new ArrowSegment(data2));
    final long originalMemory = memorySpace.getAllocator().getAllocatedMemory();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 执行join逻辑
    RowHeader rowHeader = buildTableRowHeader("t1", "test", table1);
    rowHeader.addRowHeader(buildTableRowHeader("t2", "test", table2), "t2");
    JoinExecutorNode joinExecutorNode = new JoinExecutorNode(null, rowHeader, joinType, joinSide,
            ImmutableList.of(Pair.of(
                    ColumnNameExpr.create(IdentifierExpr.create(ImmutableList.of("t1", "id"))),
                    ColumnNameExpr.create(IdentifierExpr.create(ImmutableList.of("t2", "id"))))));
    joinExecutorNode.setExchangeServiceList(ImmutableList.of(memoryDataFetcher1,
            memoryDataFetcher2));
    List<VectorSchemaRoot> result = ((ArrowSegment) joinExecutorNode.fetchData(memorySpace)).get();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 检查结果，过滤后内存要小于或等于过滤前内存.
    assert memorySpace.getAllocator().getAllocatedMemory() <= originalMemory;
    Assert.assertEquals(except,
            result.stream().map(VectorSchemaRoot::contentToTSVString).toList().toString());
    AutoCloseables.close(result);
    allocator.close();
  }
}
