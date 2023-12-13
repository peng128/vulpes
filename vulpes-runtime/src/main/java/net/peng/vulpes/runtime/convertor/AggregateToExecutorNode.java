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
import net.peng.vulpes.parser.utils.FunctionUtils;
import net.peng.vulpes.runtime.physics.AggregateExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import org.apache.commons.lang3.tuple.Pair;

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
    final Pair<Boolean, List<RelalgExpr>> aggFunctionPair =
        FunctionUtils.allAggFunctions(relalgAggregation.getAggFunctions());
    if (!aggFunctionPair.getLeft()) {
      throw new ComputeException("%s 这些函数不是聚合函数，不能出现在聚合算子中.",
          aggFunctionPair.getRight());
    }
    return new AggregateExecutorNode(nextNode, groupByIndexes, relalgAggregation.getAggFunctions(),
            relalgAggregation.getInput().getRowHeader(), relalgAggregation.getRowHeader());
  }

  @Override
  public boolean isMatch(RelalgNode relalgNode) {
    return relalgNode instanceof RelalgAggregation;
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
