package net.peng.vulpes.runtime.physics;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.function.execute.arrow.ArrowExecuteTools;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.base.ComputeExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.struct.data.ArrowSegment;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.util.AutoCloseables;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of ProjectionExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProjectionExecutorNode extends ComputeExecutorNode {

  protected final List<RelalgExpr> relalgExprList;

  public ProjectionExecutorNode(ExecutorNode next, List<RelalgExpr> relalgExprList,
                                RowHeader inputRowHeader,
                                RowHeader outputRowHeader) {
    super(next, inputRowHeader, outputRowHeader);
    this.relalgExprList = relalgExprList;
  }

  @Override
  public Segment<?> executeSingleInput(Segment<?> segment,
                                       MemorySpace memorySpace) {
    if (ObjectUtils.isNull(segment)) {
      List<FieldVector> fieldVectors = new ArrayList<>();
      for (RelalgExpr expr : relalgExprList) {
        fieldVectors.add(ArrowExecuteTools.computeExpr(expr, null, memorySpace, inputRowHeader));
      }
      return new ArrowSegment(List.of(new VectorSchemaRoot(fieldVectors)));
    }

    if (!(segment instanceof ArrowSegment arrowSegment)) {
      throw new ComputeException("不认识数据类型. %s", segment.getClass().getName());
    }
    List<VectorSchemaRoot> vectorSchemaRoots = arrowSegment.get();
    List<VectorSchemaRoot> data = new ArrayList<>();
    for (VectorSchemaRoot vectorSchemaRoot : vectorSchemaRoots) {
      List<FieldVector> fieldVectors = new ArrayList<>();
      for (RelalgExpr expr : relalgExprList) {
        fieldVectors.add(ArrowExecuteTools.computeExpr(expr, vectorSchemaRoot.getFieldVectors(),
                memorySpace, inputRowHeader));
      }
      data.add(new VectorSchemaRoot(fieldVectors));
    }
    try {
      AutoCloseables.close(vectorSchemaRoots);
    } catch (Exception e) {
      throw new ComputeException("中间结果数据无法关闭.", e);
    }
    return new ArrowSegment(data);
  }


}
