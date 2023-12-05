package net.peng.vulpes.runtime.convertor;

import java.util.ArrayList;
import java.util.List;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.function.OperatorSymbol;
import net.peng.vulpes.parser.algebraic.logical.RelalgJoin;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.runtime.exchange.MemoryDataFetcher;
import net.peng.vulpes.runtime.physics.JoinExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description of JoinToExecutorNode.
 * 将{@link net.peng.vulpes.parser.algebraic.logical.RelalgJoin} 转换为{@link ExecutorNode}.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/14
 */
public class JoinToExecutorNode implements PhysicsConvertor {

  public static final PhysicsConvertor CONVERTOR = new JoinToExecutorNode();

  @Override
  public ExecutorNode convert(RelalgNode relalgNode, Config config, ExecutorNode nextNode) {
    final RelalgJoin relalgJoin = (RelalgJoin) relalgNode;
    return new JoinExecutorNode(nextNode, relalgJoin.getRowHeader(),
            relalgJoin.getJoinType(), relalgJoin.getJoinSide(),
            convertCondition(relalgJoin.getCondition()));
  }

  @Override
  public boolean isMatch(RelalgNode relalgNode) {
    return relalgNode instanceof RelalgJoin;
  }

  /**
   * 识别一下join条件.
   * 目前只支持AND连接多字段条件，字段条件只支持等于.
   * a.col1 = b.col2 and a.col3 = b.col4
   */
  private List<Pair<ColumnNameExpr, ColumnNameExpr>> convertCondition(RelalgExpr relalgExpr) {
    final List<Pair<ColumnNameExpr, ColumnNameExpr>> conditions = new ArrayList<>();
    if (!(relalgExpr instanceof FunctionRef functionRef)) {
      throw new ComputeException("join的条件需要是一个对比方法(=) 或逻辑连接(AND), 目前是:%s", relalgExpr);
    }
    if (functionRef.getOperator().equalsIgnoreCase(OperatorSymbol.AND.value)) {
      functionRef.getItems().forEach(item -> conditions.addAll(convertCondition(item)));
    } else {
      conditions.add(convertSingleCondition(functionRef));
    }
    return conditions;
  }

  private Pair<ColumnNameExpr, ColumnNameExpr> convertSingleCondition(RelalgExpr relalgExpr) {
    if (!(relalgExpr instanceof FunctionRef functionRef)) {
      throw new ComputeException("join的条件需要是一个对比方法(=), 目前是:%s", relalgExpr);
    }
    if (!functionRef.getOperator().equalsIgnoreCase(OperatorSymbol.EQUALS.value)
            || functionRef.getItems().size() != 2) {
      throw new ComputeException("join的条件目前只支持等于， 目前是:%s", functionRef.getOperator());
    }
    if (!functionRef.getItems().stream().allMatch(item -> item instanceof ColumnNameExpr)) {
      throw new ComputeException("目前join两侧只支持字段输入.", functionRef);
    }
    return Pair.of((ColumnNameExpr) functionRef.getItems().get(0),
            (ColumnNameExpr) functionRef.getItems().get(0));
  }
}
