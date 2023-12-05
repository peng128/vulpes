package net.peng.vulpes.runtime.convertor;

import com.google.common.collect.ImmutableList;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.LiteralExpr;
import net.peng.vulpes.parser.algebraic.expression.NumericExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.function.OperatorSymbol;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgSelection;
import net.peng.vulpes.runtime.physics.SearchExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;

/**
 * Description of FilterToSearchExecutor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/25
 */
public class SelectionToSearchExecutor implements PhysicsConvertor {

  public static final PhysicsConvertor CONVERTOR = new SelectionToSearchExecutor();

  @Override
  public ExecutorNode convert(RelalgNode relalgNode, Config config, ExecutorNode nextNode) {
    RelalgSelection relalgSelection = (RelalgSelection) relalgNode;
    //TODO 这里先实现只有一个相等操作的谓词.
    FunctionRef functionRef = check(relalgSelection);
    ColumnNameExpr columnNameExpr = (ColumnNameExpr) functionRef.getItems().get(0);
    return new SearchExecutorNode(nextNode,
            columnNameExpr.getName(), columnNameExpr.getIndex(),
            ImmutableList.of(getObject(functionRef.getItems().get(1))),
            relalgSelection.getInput().getRowHeader(), relalgSelection.getRowHeader());
  }

  private Object getObject(RelalgExpr expr) {
    if (expr instanceof NumericExpr numericExpr) {
      return numericExpr.getNumeric();
    } else if (expr instanceof LiteralExpr literalExpr) {
      return literalExpr.getLiteral();
    } else {
      throw new ComputeException("谓词计算目前不支持输入类型: %s", expr.getClass().getName());
    }
  }

  private FunctionRef check(RelalgSelection relalgSelection) {
    if (!(relalgSelection.getPredicate() instanceof FunctionRef functionRef)) {
      throw new ComputeException("目前只支持表达式谓词");
    }
    if (!functionRef.getOperator().equals(OperatorSymbol.EQUALS.value)
            || functionRef.getItems().size() != 2) {
      throw new ComputeException("目前谓词只支持一个等于");
    }
    if (!(functionRef.getItems().get(0) instanceof ColumnNameExpr)) {
      throw new ComputeException("目前只支持谓词左边是一个字段输入");
    }
    if (!(functionRef.getItems().get(1) instanceof LiteralExpr)
            && !(functionRef.getItems().get(1) instanceof NumericExpr)) {
      throw new ComputeException("目前只支持谓词右侧是一个字符串或数字输入");
    }
    return functionRef;
  }

  @Override
  public boolean isMatch(RelalgNode relalgNode) {
    return relalgNode instanceof RelalgSelection;
  }
}
