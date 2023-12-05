package net.peng.vulpes.runtime.physics;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.function.execute.arrow.ArrowExecuteTools;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of ProjectionOnlyExecutorNode.
 * 用来处理project节点下没有scan节点的执行计划.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/27
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProjectionOnlyExecutorNode extends ProjectionExecutorNode {

  public ProjectionOnlyExecutorNode(ExecutorNode next, List<RelalgExpr> relalgExprList,
                                    RowHeader inputRowHeader,
                                    RowHeader outputRowHeader) {
    super(next, relalgExprList, inputRowHeader, outputRowHeader);
  }

  @Override
  public Segment<?> executeSingleInput(Segment<?> segment,
                                       MemorySpace memorySpace) {
    List<FieldVector> fieldVectors = new ArrayList<>();
    for (RelalgExpr expr : relalgExprList) {
      fieldVectors.add(ArrowExecuteTools.computeExpr(expr, null, memorySpace, inputRowHeader));
    }
    return new ArrowSegment(List.of(new VectorSchemaRoot(fieldVectors)));
  }
}
