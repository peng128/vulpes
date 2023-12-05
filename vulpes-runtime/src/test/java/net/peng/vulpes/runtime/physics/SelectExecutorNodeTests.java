package net.peng.vulpes.runtime.physics;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.expression.NumericExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
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
 * Description of SeachExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/18
 */
public class SelectExecutorNodeTests extends PhysicsNodeTestBase {

  @Test
  public void test() throws Exception {
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    // 准备数据
    List<VectorSchemaRoot> data = readData("data/table1.csv", FileFormat.CSV, memorySpace);
    final long originalMemory = memorySpace.getAllocator().getAllocatedMemory();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    final SessionManager sessionManager = SessionManager.DEFAULT_SESSION_MANAGER;
    // 执行过滤逻辑
    RowHeader rowHeader = getTableRowHeader();
    RelalgExpr predicate = FunctionRef.create("or", sessionManager,
            FunctionRef.create("=", sessionManager, ColumnNameExpr.create(IdentifierExpr.create(
                    "age")), NumericExpr.create("24")),
            FunctionRef.create("=", sessionManager, ColumnNameExpr.create(IdentifierExpr.create(
                    "age")), NumericExpr.create("18")));
    predicate.fillColumnInfo(rowHeader);
    SelectionExecutorNode selectionExecutorNode = new SelectionExecutorNode(null, predicate,
            rowHeader, rowHeader);
    List<VectorSchemaRoot> result =
            ((ArrowSegment) selectionExecutorNode.executeSingleInput(new ArrowSegment(data),
                    memorySpace)).get();
    System.out.println(memorySpace.getAllocator().getAllocatedMemory() + " bytes");
    // 检查结果，过滤后内存要小于或等于过滤前内存.
    assert memorySpace.getAllocator().getAllocatedMemory() <= originalMemory;
    Assert.assertEquals("""
            [id\t name\t age\t gender\t phone
            1\t joe\t24\t male\t151
            4\t ham\t24\t male\t155
            , id\t name\t age\t gender\t phone
            3\t cloud\t18\t male\t1513
            ]""", result.stream().map(VectorSchemaRoot::contentToTSVString).toList().toString());
    AutoCloseables.close(result);
    allocator.close();
  }
}
