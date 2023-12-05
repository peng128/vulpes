package net.peng.vulpes.runtime.physics;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
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
 * Description of ProjectionExecutorNodeTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class ProjectionExecutorNodeTests extends PhysicsNodeTestBase {

  @Test
  public void test() throws Exception {
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    // 准备数据
    List<VectorSchemaRoot> data = readData("data/table1.csv", FileFormat.CSV, memorySpace);
    final long originalMemory = memorySpace.getAllocator().getAllocatedMemory();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 执行投影逻辑
    RowHeader inputRowHeader = getTableRowHeader();
    RelalgExpr relalgExpr = ColumnNameExpr.create(IdentifierExpr.create("age"));
    relalgExpr.fillColumnInfo(inputRowHeader);
    ProjectionExecutorNode projectionExecutorNode = new ProjectionExecutorNode(null,
            ImmutableList.of(relalgExpr), inputRowHeader,
            new RowHeader(ImmutableList.of(ColumnInfo.builder().name("age")
                    .dataType(new IntType()).build())));
    List<VectorSchemaRoot> result = ((ArrowSegment)
            projectionExecutorNode.executeSingleInput(new ArrowSegment(data),
                    memorySpace)).get();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 检查结果，过滤后内存要小于或等于过滤前内存.
    assert memorySpace.getAllocator().getAllocatedMemory() <= originalMemory;
    Assert.assertEquals("""
            [ age
            24
            41
            24
            ,  age
            30
            18
            ]""", result.stream().map(VectorSchemaRoot::contentToTSVString).toList().toString());
    AutoCloseables.close(result);
    allocator.close();
  }
}
