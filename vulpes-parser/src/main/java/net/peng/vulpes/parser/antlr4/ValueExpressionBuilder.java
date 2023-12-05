package net.peng.vulpes.parser.antlr4;

import java.util.Arrays;
import java.util.List;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.LiteralExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.function.OperatorSymbol;
import net.peng.vulpes.parser.antlr4.SQL92Parser.ValueExpressionContext;

/**
 * 构建值表达式的构建器.
 */
public class ValueExpressionBuilder {

  /**
   * 计算法则高优先级计算的算法符号.
   */
  private static final List<String> HIGH_PRIORITY_CALC =
          Arrays.asList(OperatorSymbol.ASTERISH.name(),
                  OperatorSymbol.SOLIDUS.name());

  /**
   * 根据输入的值表达式上下文，计算关系表达式.
   *
   * @param ctx     值表达式
   * @param visitor 迭代器
   * @return 关系表达式
   */
  public static RelationAlgebraic build(ValueExpressionContext ctx,
                                        SQL92ParserBaseVisitor<RelationAlgebraic> visitor,
                                        SessionManager sessionManager) {
    // 没有嵌套，直接返回
    if (ObjectUtils.isEmpty(ctx.valueExpression())) {
      return visitor.visitChildren(ctx);
    }
    // 只有一个,且带有括弧。如(a+b)这种计算
    if (ctx.valueExpression().size() == 1 && ObjectUtils.isNotNull(ctx.LEFT_PAREN())
            && ObjectUtils.isNotNull(ctx.RIGHT_PAREN())) {
      RelationAlgebraic subExpr = ctx.valueExpression(0).accept(visitor);
      if (subExpr instanceof FunctionRef) {
        FunctionRef subFunctionRef = (FunctionRef) subExpr;
        subFunctionRef.addPriority();
      }
      return subExpr;
    }
    // 如果不是二元计算就直接生成表达式
    if (ctx.valueExpression().size() != 2 || ctx.getChildCount() != 3) {
      throw new AstConvertorException(
              "The number of value expression inside another value expression must less than or "
                      + "equals to 2.");
    }
    RelalgExpr left = ObjectUtils.checkClass(ctx.getChild(0).accept(visitor), RelalgExpr.class,
            AstConvertorException.class);
    LiteralExpr operator = ObjectUtils.checkClass(ctx.getChild(1).accept(visitor),
            LiteralExpr.class,
            AstConvertorException.class);
    RelalgExpr right = ObjectUtils.checkClass(ctx.getChild(2).accept(visitor), RelalgExpr.class,
            AstConvertorException.class);
    FunctionRef functionRef = FunctionRef.create(operator.getLiteral(), sessionManager, left,
            right);
    if (HIGH_PRIORITY_CALC.contains(operator.getLiteral())) {
      functionRef.addPriority();
    }
    return functionRef;
  }
}
