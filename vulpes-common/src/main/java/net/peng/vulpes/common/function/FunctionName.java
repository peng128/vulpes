package net.peng.vulpes.common.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description of FunctionName.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FunctionName {
  /**
   * 方法名称.
   */
  String name();

  /**
   * 操作符号的类型。
   * 0 - 普通函数 指 ${functionName}(param1, param2 ...)
   * 1 - 二元计算操作 指 param1 ${functionName} param2。 比如 1 + 1
   * 2 - 特殊函数表达式 函数中有特殊格式的。比如cast(col as type); case when 等。
   */
  int kind() default 0;
}
