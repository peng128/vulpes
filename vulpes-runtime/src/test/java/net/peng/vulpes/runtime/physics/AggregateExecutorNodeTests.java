package net.peng.vulpes.runtime.physics;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.peng.vulpes.common.function.aggregate.SumFunction;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.PhysicsNodeTestBase;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import org.apache.arrow.dataset.file.FileFormat;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of AggregateExecutorNodeTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
public class AggregateExecutorNodeTests extends PhysicsNodeTestBase {

  @Test
  public void test() throws Exception {
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    // 准备数据
    final List<VectorSchemaRoot> data = readData("data/table1.csv", FileFormat.CSV, memorySpace);
    final long originalMemory = memorySpace.getAllocator().getAllocatedMemory();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 执行聚合逻辑
    RowHeader rowHeader = getTableRowHeader();
    ColumnNameExpr idColumn = ColumnNameExpr.create(IdentifierExpr.create("id"));
    idColumn.fillColumnInfo(rowHeader);
    //聚合函数
    final FunctionRef sum = FunctionRef.create("sum", SessionManager.builder().build(), idColumn);
    sum.fillColumnInfo(rowHeader);
    AggregateExecutorNode aggregateExecutorNode = new AggregateExecutorNode(null,
        ImmutableList.of(2),
        ImmutableList.of(sum),
        rowHeader, new RowHeader(ImmutableList.of(
        ColumnInfo.builder().name("age").dataType(new BigIntType()).build(),
        ColumnInfo.builder().name("sum_col").dataType(new BigIntType()).build())));
    List<VectorSchemaRoot> result =
        ((ArrowSegment) aggregateExecutorNode.executeSingleInput(new ArrowSegment(data),
            memorySpace)).get();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 检查结果，过滤后内存要小于或等于过滤前内存.
    assert memorySpace.getAllocator().getAllocatedMemory() <= originalMemory;
    Assert.assertEquals("""
        [age\tAGG_OUTPUT_0
        18\t3
        24\t5
        30\t2
        , age\tAGG_OUTPUT_0
        41\t5
        ]""", result.stream().map(VectorSchemaRoot::contentToTSVString).toList().toString());
    AutoCloseables.close(result);
    allocator.close();
  }

  @Test
  public void test2() throws Exception {
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    // 准备数据
    final List<VectorSchemaRoot> data = readData("data/lineitem.arrow", FileFormat.ARROW_IPC,
        memorySpace);
    final long originalMemory = memorySpace.getAllocator().getAllocatedMemory();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 执行聚合逻辑
    RowHeader rowHeader = getTableRowHeader();
    ColumnNameExpr idColumn = ColumnNameExpr.create(IdentifierExpr.create("id"));
    idColumn.fillColumnInfo(rowHeader);
    //聚合函数
    final FunctionRef sum = FunctionRef.create("sum", SessionManager.builder().build(), idColumn);
    final FunctionRef count = FunctionRef.create("count", SessionManager.builder().build());
    sum.fillColumnInfo(rowHeader);
    count.fillColumnInfo(rowHeader);
    Stopwatch stopwatch = Stopwatch.createStarted();
    AggregateExecutorNode aggregateExecutorNode = new AggregateExecutorNode(null,
        ImmutableList.of(),
        ImmutableList.of(count),
        rowHeader, new RowHeader(ImmutableList.of(
        ColumnInfo.builder().name("sum_col").dataType(new BigIntType()).build())));
    final List<VectorSchemaRoot> result =
        ((ArrowSegment) aggregateExecutorNode.executeSingleInput(new ArrowSegment(data),
            memorySpace)).get();
    System.out.println(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 检查结果，过滤后内存要小于或等于过滤前内存.
    assert memorySpace.getAllocator().getAllocatedMemory() <= originalMemory;
    Assert.assertEquals("""
        [AGG_OUTPUT_0
        6001215
        ]""", result.stream().map(VectorSchemaRoot::contentToTSVString).toList().toString());
    AutoCloseables.close(result);
    allocator.close();
    System.out.println(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
  }
}
