package net.peng.vulpes.parser.algebraic.function;

import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.BooleanType;
import net.peng.vulpes.common.type.IntType;

/**
 * 这个类描述了计算方法的函数名称.使用{@link net.peng.vulpes.common.function.Function}代替.
 */
@Deprecated
public enum OperatorSymbol {
  PLUS("+", 1, TypeSpeculate.create(0)),
  MINUS("-", 1, TypeSpeculate.create(0)),
  ASTERISH("*", 1, TypeSpeculate.create(0)),
  SOLIDUS("/", 1, TypeSpeculate.create(0)),
  AND("AND", 1, TypeSpeculate.create(new BooleanType())),
  OR("OR", 1, TypeSpeculate.create(new BooleanType())),
  NOT("NOT", 2, TypeSpeculate.create(new BooleanType())),
  CAST("CAST", 2, TypeSpeculate.create(0)),
  IS("IS", 2, TypeSpeculate.create(new BooleanType())),
  IS_NOT("IS_NOT", 2, TypeSpeculate.create(new BooleanType())),
  MOD("%", 1, TypeSpeculate.create(new IntType())),
  EQUALS("=", 1, TypeSpeculate.create(new BooleanType())),
  GREATE_THAN(">=", 1, TypeSpeculate.create(new BooleanType())),
  GREATE(">", 1, TypeSpeculate.create(new BooleanType())),
  LESS_THAN("<", 1, TypeSpeculate.create(new BooleanType())),
  LESS("<", 1, TypeSpeculate.create(new BooleanType())),
  /**
   * 简单case when函数.例如
   * case a when 1 then xxx else yyy end
   */
  CASE_SIMPLE("case_simple", 2, TypeSpeculate.create(2)),
  /**
   * 普通case when. 例如
   * case when a = 1 then xxx else yyy end
   */
  CASE("case", 2, TypeSpeculate.create(1)),

  SUM("sum", 0, TypeSpeculate.create(new BigIntType()));

  public final String value;
  /**
   * 操作符号的类型。
   * 0 - 普通函数 指 ${functionName}(param1, param2 ...)
   * 1 - 二元计算操作 指 param1 ${functionName} param2。 比如 1 + 1
   * 2 - 特殊函数表达式 函数中有特殊格式的。比如cast(col as type); case when 等。
   */
  public final int kind;

  /**
   * 如何进行函数结构类型推断的策略.
   */
  public final TypeSpeculate typeSpeculate;

  OperatorSymbol(String value, int kind, TypeSpeculate typeSpeculate) {
    this.value = value;
    this.kind = kind;
    this.typeSpeculate = typeSpeculate;
  }
}
