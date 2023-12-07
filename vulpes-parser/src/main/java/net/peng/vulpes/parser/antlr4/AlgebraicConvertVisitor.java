package net.peng.vulpes.parser.antlr4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.peng.vulpes.catalog.manager.CatalogManager;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.model.TableIdentifier;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.AliasExpr;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FullColumnRef;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.expression.LiteralExpr;
import net.peng.vulpes.parser.algebraic.expression.NumericExpr;
import net.peng.vulpes.parser.algebraic.expression.ParameterExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.expression.SortExpr;
import net.peng.vulpes.parser.algebraic.expression.SortExpr.SortKind;
import net.peng.vulpes.parser.algebraic.function.OperatorSymbol;
import net.peng.vulpes.parser.algebraic.logical.RelalgAggregation;
import net.peng.vulpes.parser.algebraic.logical.RelalgAlias;
import net.peng.vulpes.parser.algebraic.logical.RelalgJoin;
import net.peng.vulpes.parser.algebraic.logical.RelalgJoin.JoinSide;
import net.peng.vulpes.parser.algebraic.logical.RelalgJoin.JoinType;
import net.peng.vulpes.parser.algebraic.logical.RelalgLimit;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgProjection;
import net.peng.vulpes.parser.algebraic.logical.RelalgScan;
import net.peng.vulpes.parser.algebraic.logical.RelalgSelection;
import net.peng.vulpes.parser.algebraic.logical.RelalgSort;
import net.peng.vulpes.parser.algebraic.logical.RelalgUnion;
import net.peng.vulpes.parser.algebraic.logical.RelalgUnion.Type;
import net.peng.vulpes.parser.algebraic.meta.RelalgSet;
import net.peng.vulpes.parser.algebraic.meta.ShowCatalogMetaNode;
import net.peng.vulpes.parser.algebraic.meta.ShowSchemaMetaNode;
import net.peng.vulpes.parser.algebraic.meta.ShowTableMetaNode;
import net.peng.vulpes.parser.antlr4.SQL92Parser.BooleanFactorContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.BooleanPrimaryContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.BooleanTermContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.BooleanTestContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.CaseExpressionContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.CastSpecificationContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.ColumnReferenceContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.ComparisonPredicateContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.FunctionContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.GroupByClauseContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.GroupingColumnReferenceContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.HavingClauseContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.JoinedTableContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.LimitClauseContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.OrderByClauseContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.QueryExpressionContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.RowValueConstructorContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.SearchConditionContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.SelectListContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.SelectSublistContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.ShowCatalogsContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.ShowSchemasContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.ShowTablesContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.SortSpecificationListContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.TableNameContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.ValueExpressionContext;
import net.peng.vulpes.parser.antlr4.SQL92Parser.WhereClauseContext;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * 把anltr4的结果转为代数表达式.
 */
public class AlgebraicConvertVisitor extends SQL92ParserBaseVisitor<RelationAlgebraic> {

  private final CatalogManager catalogManager;

  private final SessionManager sessionManager;

  public AlgebraicConvertVisitor(CatalogManager catalogManager, SessionManager sessionManager) {
    this.catalogManager = catalogManager;
    this.sessionManager = sessionManager;
  }

  @Override
  public RelationAlgebraic visitSetSpecification(SQL92Parser.SetSpecificationContext ctx) {
    return new RelalgSet(ctx.parameterName.getText(), ctx.parameterValue.getText());
  }

  /**
   * 显示目录列表.
   */
  @Override
  public RelationAlgebraic visitShowCatalogs(ShowCatalogsContext ctx) {
    return new ShowCatalogMetaNode();
  }

  /**
   * 显示当前目录下数据库列表.
   */
  @Override
  public RelationAlgebraic visitShowSchemas(ShowSchemasContext ctx) {
    return new ShowSchemaMetaNode();
  }

  /**
   * 显示当前数据库下表列表.
   */
  @Override
  public RelationAlgebraic visitShowTables(ShowTablesContext ctx) {
    return new ShowTableMetaNode();
  }

  /**
   * 合并两个子树.
   *
   * @param aggregate  当前子树.
   * @param nextResult 下一个子树.
   * @return 返回合并后的子树.
   */
  @Override
  protected RelationAlgebraic aggregateResult(final RelationAlgebraic aggregate,
                                              final RelationAlgebraic nextResult) {
    if (ObjectUtils.isNotNull(aggregate)) {
      if (ObjectUtils.isNull(nextResult)) {
        return aggregate;
      }
      return aggregate.merge(nextResult);
    }
    return nextResult;
  }

  /**
   * visitSelectList.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitSelectList(final SelectListContext ctx) {
    RelalgProjection.Builder builder = new RelalgProjection.Builder();
    for (SelectSublistContext selectSublistContext : ctx.selectSublist()) {
      builder.addItem(castToRelalgExpr(selectSublistContext.accept(this)));
    }
    return builder.build();
  }

  /**
   * visitSelectSublist.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitSelectSublist(final SelectSublistContext ctx) {
    if (ObjectUtils.isNotNull(ctx.AS()) && ObjectUtils.isNotNull(ctx.IDENTIFIER())) {
      return AliasExpr.create(castToRelalgExpr(ctx.valueExpression().accept(this)),
              ctx.IDENTIFIER().accept(this).toString());
    }
    if (ObjectUtils.isNotNull(ctx.ASTERISK())) {
      return new FullColumnRef();
    }
    return super.visitSelectSublist(ctx);
  }

  /**
   * visitQueryExpression.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitQueryExpression(final QueryExpressionContext ctx) {
    if (ctx.queryExpression().size() <= 1) {
      return super.visitQueryExpression(ctx);
    }

    if (ObjectUtils.isNull(ctx.left) || ObjectUtils.isNull(ctx.right)) {
      return super.visitQueryExpression(ctx);
    }
    RelalgNode left = castToRelalgNode(ctx.left.accept(this));
    RelalgNode right = castToRelalgNode(ctx.right.accept(this));
    boolean all = false;
    if (ObjectUtils.isNotNull(ctx.ALL())) {
      all = true;
    }
    RelalgUnion.Type type = getUnionType(ctx);
    // 如果输入中已经有一个union了，并且all的选项是相同的，则可以合并。
    List<RelalgNode> inputs = new ArrayList<>(flatInput(left, all, type));
    inputs.addAll(flatInput(right, all, type));
    return RelalgUnion.create(all, type, inputs);
  }

  /**
   * 如果传入节点是union节点，且属性和传入相同。则将这个union展平，取其输入.
   *
   * @param relalgNode 代数表达式
   * @param all        是否去重（true-不去重/false-去重）
   * @param type       关联类型
   * @return 展平后union代数表达式.
   */
  private List<RelalgNode> flatInput(final RelalgNode relalgNode, final boolean all,
                                     final RelalgUnion.Type type) {
    if (relalgNode instanceof RelalgUnion) {
      RelalgUnion unionNode = (RelalgUnion) relalgNode;
      if (type.equals(unionNode.getType()) && all == unionNode.isAll()) {
        return unionNode.getInputs();
      }
    }
    return Collections.singletonList(relalgNode);
  }

  /**
   * 从AST中获取union类型.
   *
   * @param ctx the parse tree
   * @return 关联类型
   */
  private RelalgUnion.Type getUnionType(final QueryExpressionContext ctx) {
    RelalgUnion.Type type = Type.UNION;
    if (ObjectUtils.isNotNull(ctx.EXCEPT())) {
      type = Type.EXCEPT;
    } else if (ObjectUtils.isNotNull(ctx.INTERSECT())) {
      type = Type.INTERSECT;
    }
    return type;
  }

  /**
   * visitJoinedTable.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitJoinedTable(final JoinedTableContext ctx) {
    if (!ObjectUtils.isEmpty(ctx.tableReference())) {
      if (ctx.tableReference().size() == 1) {
        //单表查询
        return super.visitJoinedTable(ctx);
      } else if (ctx.tableReference().size() == 2) {
        return RelalgJoin.create(JoinType.CROSS_JOIN, JoinSide.FULL,
                castToRelalgNode(ctx.tableReference(0).accept(this)),
                castToRelalgNode(ctx.tableReference(1).accept(this)), null);
      } else {
        throw new AstConvertorException("Except 2 table reference in joined table， but %s.",
                ctx.tableReference().size());
      }
    }
    if (ctx.joinedTable().size() == 1) {
      return super.visitJoinedTable(ctx);
    }
    if (ctx.joinedTable().size() != 2) {
      throw new AstConvertorException("Except 2 joined table in joined table， but %s.",
              ctx.joinedTable().size());
    }
    final RelalgNode left = castToRelalgNode(ctx.joinedTable(0).accept(this));
    final RelalgNode right = castToRelalgNode(ctx.joinedTable(1).accept(this));
    RelalgJoin.JoinSide joinSide = JoinSide.FULL;
    RelalgJoin.JoinType joinType = JoinType.CROSS_JOIN;
    RelalgExpr condition = null;
    if (ObjectUtils.isNotNull(ctx.JOIN())) {
      // 默认是inner join
      joinType = JoinType.INNER_JOIN;
    }
    if (ObjectUtils.isNotNull(ctx.joinType())
            && ObjectUtils.isNotNull(ctx.joinType().outerJoinType())) {
      joinType = JoinType.OUTER_JOIN;
      if (ObjectUtils.isNotNull(ctx.joinType().outerJoinType().LEFT())) {
        joinSide = JoinSide.LEFT;
      }
      if (ObjectUtils.isNotNull(ctx.joinType().outerJoinType().RIGHT())) {
        joinSide = JoinSide.RIGHT;
      }
    }
    if (ObjectUtils.isNotNull(ctx.joinSpecification())) {
      condition = castToRelalgExpr(ctx.joinSpecification().accept(this));
    }
    return RelalgJoin.create(joinType, joinSide, left, right, condition);
  }

  /**
   * visitFunction.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitFunction(final FunctionContext ctx) {
    FunctionRef.Builder builder = new FunctionRef.Builder();
    for (ValueExpressionContext valueExpressionContext : ctx.valueExpression()) {
      builder.addItem(castToRelalgExpr(valueExpressionContext.accept(this)));
    }
    builder.operator(ctx.functionName().accept(this).toString())
            .sessionManager(sessionManager);
    return builder.build();
  }

  /**
   * visitCastSpecification.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitCastSpecification(final CastSpecificationContext ctx) {
    RelalgExpr input;
    if (ObjectUtils.isNotNull(ctx.NULL())) {
      input = castToRelalgExpr(ctx.NULL().accept(this));
    } else {
      input = castToRelalgExpr(ctx.valueExpression().accept(this));
    }
    return FunctionRef.create(OperatorSymbol.CAST.name(), sessionManager, input,
            castToRelalgExpr(ctx.dataType().accept(this)));
  }

  /**
   * visitCaseExpression.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitCaseExpression(final CaseExpressionContext ctx) {
    FunctionRef.Builder builder = new FunctionRef.Builder();
    if (!ObjectUtils.isEmpty(ctx.searchCondition())) {
      builder.operator(OperatorSymbol.CASE.name());
      for (SearchConditionContext searchConditionContext : ctx.searchCondition()) {
        builder.addItem(castToRelalgExpr(searchConditionContext.accept(this)));
      }
    } else {
      builder.operator(OperatorSymbol.CASE_SIMPLE.name());
    }
    for (ValueExpressionContext valueExpressionContext : ctx.valueExpression()) {
      builder.addItem(castToRelalgExpr(valueExpressionContext.accept(this)));
    }
    return builder.sessionManager(sessionManager).build();
  }

  /**
   * ValueExpressionContext.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitValueExpression(final ValueExpressionContext ctx) {
    return ValueExpressionBuilder.build(ctx, this, sessionManager);
  }

  /**
   * ColumnReferenceContext.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitColumnReference(final ColumnReferenceContext ctx) {
    RelationAlgebraic relationAlgebraic = super.visitColumnReference(ctx);
    if (!(relationAlgebraic instanceof IdentifierExpr)) {
      throw new AstConvertorException("There isn't any identifier in column name");
    }
    return ColumnNameExpr.create((IdentifierExpr) relationAlgebraic);
  }

  /**
   * ParameterReferenceContext.
   * 这里直接返回变量的值.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitParameterReference(SQL92Parser.ParameterReferenceContext ctx) {
    RelationAlgebraic relationAlgebraic = ctx.columnReference().accept(this);
    return ParameterExpr.create(ObjectUtils.checkClass(relationAlgebraic,
            ColumnNameExpr.class, AstConvertorException.class), sessionManager);
  }

  /**
   * visitTableName.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitTableName(final TableNameContext ctx) {
    RelationAlgebraic relationAlgebraic = super.visitTableName(ctx);
    if (!(relationAlgebraic instanceof IdentifierExpr)) {
      throw new AstConvertorException("There isn't any identifier in table name");
    }
    List<String> identifierList = ((IdentifierExpr) relationAlgebraic).getIdentifiers();
    TableIdentifier tableIdentifier = TableIdentifier.create(identifierList);
    if (ObjectUtils.isNotNull(sessionManager)) {
      tableIdentifier = TableIdentifier.create(identifierList, sessionManager.getCurrentCatalog(),
              sessionManager.getCurrentSchema());
    }
    if (ObjectUtils.isNull(catalogManager)) {
      return RelalgScan.create(tableIdentifier);
    } else {
      return RelalgScan.create(tableIdentifier, catalogManager.getTable(tableIdentifier));
    }
  }

  @Override
  public RelationAlgebraic visitAliasName(SQL92Parser.AliasNameContext ctx) {
    return RelalgAlias.create(ctx.IDENTIFIER().getText());
  }

  /**
   * visitGroupByClause.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitGroupByClause(final GroupByClauseContext ctx) {
    List<RelalgExpr> groupBys = new ArrayList<>();
    for (GroupingColumnReferenceContext groupingColumnReferenceContext :
            ctx.groupingColumnReference()) {
      RelationAlgebraic relationAlgebraic = groupingColumnReferenceContext.accept(this);
      // TODO: 这里检查一下要是聚合函数
      groupBys.add(castToRelalgExpr(relationAlgebraic));
    }
    return RelalgAggregation.create(new ArrayList<>(), groupBys);
  }

  /**
   * visitHavingClause.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitHavingClause(final HavingClauseContext ctx) {
    return RelalgSelection.create(castToRelalgExpr(super.visitHavingClause(ctx)));
  }

  /**
   * visitWhereClause.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitWhereClause(final WhereClauseContext ctx) {
    return RelalgSelection.create(castToRelalgExpr(super.visitWhereClause(ctx)));
  }

  /**
   * visitSearchCondition.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitSearchCondition(final SearchConditionContext ctx) {
    // search condition 里面只有or
    if (ObjectUtils.isNull(ctx.searchCondition())) {
      return super.visitSearchCondition(ctx);
    }
    return FunctionRef.create(OperatorSymbol.OR.name(), sessionManager,
            castToRelalgExpr(ctx.searchCondition().accept(this)),
            castToRelalgExpr(ctx.booleanTerm().accept(this)));
  }

  /**
   * visitBooleanTerm.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitBooleanTerm(final BooleanTermContext ctx) {
    // boolean term 只有and
    if (ObjectUtils.isNull(ctx.booleanTerm())) {
      return super.visitBooleanTerm(ctx);
    }
    return FunctionRef.create(OperatorSymbol.AND.name(), sessionManager,
            castToRelalgExpr(ctx.booleanTerm().accept(this)),
            castToRelalgExpr(ctx.booleanFactor().accept(this)));
  }

  /**
   * visitBooleanFactor.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitBooleanFactor(final BooleanFactorContext ctx) {
    // boolean factor 只有not
    if (ObjectUtils.isNull(ctx.NOT())) {
      return super.visitBooleanFactor(ctx);
    }
    return FunctionRef.create(OperatorSymbol.NOT.name(), sessionManager,
            castToRelalgExpr(ctx.booleanTest().accept(this)));
  }

  /**
   * visitBooleanTest.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitBooleanTest(final BooleanTestContext ctx) {
    // is not unknown / ture / false
    if (ObjectUtils.isNull(ctx.IS())) {
      return super.visitBooleanTest(ctx);
    }
    if (ObjectUtils.isNull(ctx.NOT())) {
      return FunctionRef.create(OperatorSymbol.IS.name(), sessionManager,
              castToRelalgExpr(ctx.booleanPrimary().accept(this)),
              castToRelalgExpr(ctx.truthValue().accept(this)));
    } else {
      return FunctionRef.create(OperatorSymbol.IS_NOT.name(), sessionManager,
              castToRelalgExpr(ctx.booleanPrimary().accept(this)),
              castToRelalgExpr(ctx.truthValue().accept(this)));
    }
  }

  /**
   * visitBooleanPrimary.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitBooleanPrimary(final BooleanPrimaryContext ctx) {
    // 括弧的判断
    if (ObjectUtils.isNull(ctx.LEFT_PAREN()) && ObjectUtils.isNull(ctx.RIGHT_PAREN())) {
      return super.visitBooleanPrimary(ctx);
    }
    RelationAlgebraic searchCondition = ctx.searchCondition().accept(this);
    if (searchCondition instanceof FunctionRef) {
      ((FunctionRef) searchCondition).addPriority();
    }
    return searchCondition;
  }

  /**
   * visitComparisonPredicate.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitComparisonPredicate(final ComparisonPredicateContext ctx) {
    FunctionRef.Builder builder = new FunctionRef.Builder();
    builder.operator(ctx.compOp().accept(this).toString());
    for (RowValueConstructorContext rowValueConstructorContext : ctx.rowValueConstructor()) {
      builder.addItem(castToRelalgExpr(rowValueConstructorContext.accept(this)));
    }
    return builder.sessionManager(sessionManager).build();
  }

  /**
   * visitOrderByClause.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitOrderByClause(final OrderByClauseContext ctx) {
    List<SortExpr> sortExprList = new ArrayList<>();
    for (SortSpecificationListContext sortSpecificationListContext : ctx.sortSpecificationList()) {
      sortExprList.add(ObjectUtils.checkClass(sortSpecificationListContext.accept(this),
              SortExpr.class, AstConvertorException.class));
    }
    return RelalgSort.create(sortExprList);
  }

  /**
   * visitSortSpecificationList.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitSortSpecificationList(final SortSpecificationListContext ctx) {
    final RelalgExpr expr = castToRelalgExpr(ctx.valueExpression().accept(this));
    if (ObjectUtils.isEmpty(ctx.DESC())) {
      return SortExpr.create(expr);
    }
    return SortExpr.create(expr, SortKind.DESC);
  }

  /**
   * visitLimitClause.
   *
   * @param ctx the parse tree
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitLimitClause(final LimitClauseContext ctx) {
    return RelalgLimit.create(Integer.parseInt(ctx.DIGITS().getText()));
  }

  /**
   * visitTerminal.
   *
   * @param node 节点
   * @return 关系代数
   */
  @Override
  public RelationAlgebraic visitTerminal(final TerminalNode node) {
    switch (node.getSymbol().getType()) {
      case SQL92Lexer.IDENTIFIER:
        return IdentifierExpr.create(node.getText());
      case SQL92Lexer.DIGITS:
        return NumericExpr.create(node.getText());
      case SQL92Lexer.SELECT:
      case SQL92Lexer.PERIOD:
      case SQL92Lexer.AS:
      case SQL92Lexer.FROM:
      case SQL92Lexer.AND:
      case SQL92Lexer.OR:
      case SQL92Lexer.WHERE:
      case SQL92Lexer.HAVING:
      case SQL92Lexer.EOF:
      case SQL92Lexer.ON:
      case SQL92Lexer.LEFT_PAREN:
      case SQL92Lexer.RIGHT_PAREN:
        return defaultResult();
      case SQL92Lexer.LITERAL:
        return LiteralExpr.create(node.getText().substring(1, node.getText().length() - 1));
      case SQL92Lexer.PLUS:
      case SQL92Lexer.MINUS:
      case SQL92Lexer.ASTERISK:
      case SQL92Lexer.SOLIDUS:
      default:
        return LiteralExpr.create(node.getText());
    }
  }

  /**
   * 转为关系表达式.
   *
   * @param relationAlgebraic 表达式
   * @return 表达式
   */
  private RelalgExpr castToRelalgExpr(final RelationAlgebraic relationAlgebraic) {
    return ObjectUtils.checkClass(relationAlgebraic, RelalgExpr.class, AstConvertorException.class);
  }

  /**
   * 转为代数表达式节点.
   *
   * @param relationAlgebraic 表达式
   * @return 关系代数节点
   */
  private RelalgNode castToRelalgNode(final RelationAlgebraic relationAlgebraic) {
    return ObjectUtils.checkClass(relationAlgebraic, RelalgNode.class, AstConvertorException.class);
  }
}
