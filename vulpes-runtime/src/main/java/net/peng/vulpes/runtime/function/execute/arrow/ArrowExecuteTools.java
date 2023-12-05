package net.peng.vulpes.runtime.function.execute.arrow;

import java.util.List;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.parser.algebraic.expression.AliasExpr;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.LiteralExpr;
import net.peng.vulpes.parser.algebraic.expression.NumericExpr;
import net.peng.vulpes.parser.algebraic.expression.ParameterExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.function.execute.ExpressionExecutor;
import net.peng.vulpes.runtime.memory.MemorySpace;
import org.apache.arrow.vector.FieldVector;

/**
 * Description of ArrowExprExecuteTools.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/30
 */
public class ArrowExecuteTools {

  /**
   * 执行表达式.
   */
  public static FieldVector computeExpr(RelalgExpr expr, List<FieldVector> data,
                                        MemorySpace memorySpace, RowHeader inputRowHeader) {
    ExpressionExecutor<List<FieldVector>, FieldVector> expressionExecutor;
    if (expr instanceof ParameterExpr parameterExpr) {
      return computeExpr(parameterExpr.getValue(), data, memorySpace, inputRowHeader);
    } else if (expr instanceof ColumnNameExpr columnNameExpr) {
      expressionExecutor = new ArrowColumnExpression(columnNameExpr.getIndex());
    } else if (expr instanceof LiteralExpr literalExpr) {
      expressionExecutor = new ArrowConstantExpression(literalExpr.getLiteral(),
              literalExpr.fillColumnInfo(inputRowHeader));
    } else if (expr instanceof NumericExpr numericExpr) {
      expressionExecutor = new ArrowConstantExpression(numericExpr.getNumeric(),
              numericExpr.fillColumnInfo(inputRowHeader));
    } else if (expr instanceof FunctionRef functionRef) {
      expressionExecutor = new ArrowScalarExpression(functionRef, inputRowHeader);
    } else if (expr instanceof AliasExpr aliasExpr) {
      return computeExpr(aliasExpr.getRelalgExpr(), data, memorySpace, inputRowHeader);
    } else {
      throw new ComputeException("无法处理函数中的表达式: %s", expr);
    }
    return expressionExecutor.execute(data, memorySpace);
  }
}
