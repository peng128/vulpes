package net.peng.vulpes.runtime.convertor;

import java.util.ArrayList;
import java.util.List;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.function.aggregate.AggregateFunction;
import net.peng.vulpes.common.function.aggregate.SumFunction;
import net.peng.vulpes.parser.algebraic.expression.AliasExpr;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.logical.RelalgAggregation;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.runtime.physics.AggregateExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;

/**
 * Description of AggregateToExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/7
 */
public class AggregateToExecutorNode implements PhysicsConvertor {

  public static final PhysicsConvertor CONVERTOR = new AggregateToExecutorNode();

  @Override
  public ExecutorNode convert(RelalgNode relalgNode, Config config, ExecutorNode nextNode) {
    RelalgAggregation relalgAggregation = (RelalgAggregation) relalgNode;
    List<Integer> groupByIndexes = getColumnIndexes(relalgAggregation.getGroupBys());
    List<AggregateFunction> aggFunctions = relalgAggregation.getAggFunctions().stream()
            .map(this::convertFunction).toList();
    return new AggregateExecutorNode(nextNode, groupByIndexes, aggFunctions,
            relalgAggregation.getInput().getRowHeader(), relalgAggregation.getRowHeader());
  }

  @Override
  public boolean isMatch(RelalgNode relalgNode) {
    return relalgNode instanceof RelalgAggregation;
  }

  private AggregateFunction convertFunction(RelalgExpr relalgExpr) {
    RelalgExpr aggRelalgExpr = relalgExpr;
    String alias = null;
    if (aggRelalgExpr instanceof AliasExpr) {
      final AliasExpr aliasExpr = (AliasExpr) aggRelalgExpr;
      alias = aliasExpr.getAlias();
      aggRelalgExpr = aliasExpr.getRelalgExpr();
    }
    if (!(aggRelalgExpr instanceof FunctionRef)) {
      throw new ComputeException("需要输入聚合函数，但找到: %s", aggRelalgExpr.toString());
    }
    FunctionRef functionRef = (FunctionRef) aggRelalgExpr;
    //TODO 这里之后写一个遍历来找对应的function实现
    if (functionRef.getOperator().equalsIgnoreCase("sum")) {
      return new SumFunction(getColumnIndexes(functionRef.getItems()), alias);
    }
    throw new ComputeException("不支持方法:%s", functionRef.toString());
  }

  private List<Integer> getColumnIndexes(List<RelalgExpr> relalgExprList) {
    List<Integer> columnIndexes = new ArrayList<>(relalgExprList.size());
    for (RelalgExpr relalgExpr : relalgExprList) {
      if (!(relalgExpr instanceof ColumnNameExpr columnNameExpr)) {
        throw new ComputeException("需要是一个列信息输入:%s", relalgExpr.toString());
      }
      columnIndexes.add(columnNameExpr.getIndex());
    }
    return columnIndexes;
  }
}
