package net.peng.vulpes.parser.algebraic.logical;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.ExpressionUtils;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 聚合关系表达式节点.
 */
@ToString
@Getter
public class RelalgAggregation extends SingleInputRelalgNode {

  private final List<RelalgExpr> aggFunctions;

  private final List<RelalgExpr> groupBys;

  private RelalgAggregation(List<RelalgExpr> aggFunctions, List<RelalgExpr> groupBys) {
    this.aggFunctions = aggFunctions;
    this.groupBys = groupBys;
  }

  public static RelalgAggregation create(List<RelalgExpr> aggFunctions, List<RelalgExpr> groupBys) {
    return new RelalgAggregation(aggFunctions, groupBys);
  }

  public static RelalgAggregation create(List<RelalgExpr> groupBys) {
    return new RelalgAggregation(null, groupBys);
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    if (!ObjectUtils.isEmpty(aggFunctions)) {
      if (ObjectUtils.isEmpty(this.getInputs())) {
        throw new AstConvertorException("Can't merge [%s] during agg-function is not empty.", this);
      }
      SingleInputRelalgNode singleInputRelalgNode = ObjectUtils.checkClass(relationAlgebraic,
              SingleInputRelalgNode.class, AstConvertorException.class);
      return singleInputRelalgNode.setInput(this);
    }
    RelalgProjection relalgProjection = ObjectUtils.checkClass(relationAlgebraic,
            RelalgProjection.class, AstConvertorException.class);
    if (ObjectUtils.isNotNull(relalgProjection.rowHeader)) {
      this.computeOutputHeader(relalgProjection.rowHeader);
    }
    Pair<List<RelalgExpr>, List<RelalgExpr>> aggFeature = splitGroupBys(this.groupBys,
            relalgProjection.getProjects());
    RelalgAggregation relalgAggregation = RelalgAggregation.create(aggFeature.getRight(),
            aggFeature.getLeft());
    return relalgAggregation.setInput(relalgProjection.getInput());
  }

  @Override
  public RelalgNode accept(RelalgNodeVisitor relalgNodeVisitor) {
    return relalgNodeVisitor.visit(this);
  }

  /**
   * 找到投影中的聚合函数引用（非聚合键的引用）.
   * 返回键值对，键位聚合键集合，值位聚合函数集合.
   */
  private Pair<List<RelalgExpr>, List<RelalgExpr>> splitGroupBys(List<RelalgExpr> groupBys,
                                                                 List<RelalgExpr> projects) {
    List<RelalgExpr> functions = new ArrayList<>(projects.size() - groupBys.size());
    List<RelalgExpr> newGroupBys = new ArrayList<>(groupBys.size());
    for (RelalgExpr project : projects) {
      if (findExprInList(project, groupBys, projects)) {
        newGroupBys.add(project);
        continue;
      }
      if (!ExpressionUtils.isFunction(project)) {
        throw new AstConvertorException("[%s] isn't a function and don't appear in group by %s.",
                project, groupBys);
      }
      functions.add(project);
    }
    return Pair.of(newGroupBys, functions);
  }

  /**
   * 在expr中ExprList是否可找到expr.
   * 如果遇到输入字段索引的形式，需要转换为原始表达式
   */
  private boolean findExprInList(RelalgExpr expr, List<RelalgExpr> relalgExprList,
                                 List<RelalgExpr> row) {
    for (RelalgExpr relalgExpr : relalgExprList) {
      RelalgExpr thisRelalgExpr = relalgExpr;
      final Optional<Integer> index = ExpressionUtils.unwrapIndexIf(relalgExpr);
      if (index.isPresent()) {
        thisRelalgExpr = row.get(index.get() - 1);
      }
      if (ExpressionUtils.equals(expr, thisRelalgExpr)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public RowHeader computeOutputHeader(RowHeader inputHeader) {
    if (ObjectUtils.isNotNull(rowHeader)) {
      return rowHeader;
    }
    List<ColumnInfo> groupBysColumnInfo = computeOutputHeaderIndividual(this.groupBys, inputHeader);
    List<ColumnInfo> aggFunctionsColumnInfo = computeOutputHeaderIndividual(this.aggFunctions,
            inputHeader);
    List<ColumnInfo> columnNameList = new ArrayList<>();
    columnNameList.addAll(groupBysColumnInfo);
    columnNameList.addAll(aggFunctionsColumnInfo);
    return new RowHeader(columnNameList);
  }

  private List<ColumnInfo> computeOutputHeaderIndividual(List<RelalgExpr> exprs,
                                                         RowHeader inputHeader) {
    return exprs.stream().map(relalgExpr ->
                    relalgExpr.fillColumnInfo(inputHeader))
            .collect(Collectors.toList());
  }
}
