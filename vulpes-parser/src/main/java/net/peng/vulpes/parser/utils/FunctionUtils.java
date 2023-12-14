package net.peng.vulpes.parser.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.function.Function;
import net.peng.vulpes.common.function.FunctionName;
import net.peng.vulpes.common.function.aggregate.AggregateFunction;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.expression.AliasExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description of FunctionUtils.
 * 函数方法工具类.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
@Slf4j
public class FunctionUtils {

  public static final String EVAL_FUNCTION_NAME = "eval";

  /**
   * 判断传入表达式是否是聚合函数.
   */
  public static Boolean isAggFunction(RelalgExpr relalgExpr) {
    if (relalgExpr instanceof AliasExpr aliasExpr) {
      return isAggFunction(aliasExpr.getRelalgExpr());
    }
    if (!(relalgExpr instanceof FunctionRef functionRef)) {
      return false;
    }
    return functionRef.getFunction() instanceof AggregateFunction;
  }

  /**
   * 检查函数输入获取函数输出类型.
   */
  public static Class<?> checkFunctionType(FunctionRef functionRef, RowHeader inputRowHeader) {
    Class<?>[] inputClass = new Class[functionRef.getItems().size()];
    // 找到并计算输入类型.
    for (int i = 0; i < functionRef.getItems().size(); i++) {
      final RelalgExpr relalgExpr = functionRef.getItems().get(i);
      if (relalgExpr instanceof FunctionRef subFunctionRef) {
        inputClass[i] = checkFunctionType(subFunctionRef, inputRowHeader);
      } else {
        inputClass[i] = relalgExpr.fillColumnInfo(inputRowHeader).getDataType().getJavaType();
      }
    }
    // 获取返回类型.
    assert functionRef.getFunction() != null;
    return getEvalMethod(functionRef.getOperator(), functionRef.getFunction(), inputClass)
        .getReturnType();
  }

  /**
   * 获取对应函数的Eval执行方法.
   */
  public static Method getEvalMethod(String functionName, Function function,
                                     Class<?>... parameterTypes) {
    return getMethod(functionName, function, EVAL_FUNCTION_NAME, parameterTypes);
  }

  /**
   * 获取对应函数的执行方法.
   */
  public static Method getMethod(String functionName, Function function, String methodName,
                                     Class<?>... parameterTypes) {
    try {
      assert function != null;
      return function.getClass().getDeclaredMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException e) {
      final Method[] declaredMethods = function.getClass().getDeclaredMethods();
      Method method = subclassCompatible(declaredMethods, methodName, parameterTypes);
      if (ObjectUtils.isNotNull(method)) {
        return method;
      }
      StringBuilder errorMessageBuilder = new StringBuilder();
      errorMessageBuilder.append("函数[").append(functionName)
              .append("]输入类型不正确,目前输入类型为\n");
      errorMessageBuilder.append(Arrays.toString(parameterTypes)).append("\n可以是以下输入:\n");
      for (Method declaredMethod : declaredMethods) {
        if (!declaredMethod.getName().equals(methodName)) {
          continue;
        }
        errorMessageBuilder.append("[");
        for (Class<?> parameterType : declaredMethod.getParameterTypes()) {
          errorMessageBuilder.append(parameterType).append("; ");
        }
        errorMessageBuilder.append("]");
      }
      log.error(errorMessageBuilder.toString());
      throw new AstConvertorException(errorMessageBuilder.toString());
    }
  }

  /**
   * 判断对应eval方法中是否包含输入类型的父类型.
   */
  private static Method subclassCompatible(Method[] declaredMethods, String methodName,
                                           Class<?>... parameterTypes) {
    for (Method method : declaredMethods) {
      if (method.getName().equalsIgnoreCase(methodName)
              && ObjectUtils.allSubClass(parameterTypes, method.getParameterTypes())) {
        return method;
      }
    }
    return null;
  }

  /**
   * 根据输入的算子名称返回函数实现.
   */
  public static Function getFunction(String operatorName, ClassLoader classLoader) {
    Class<?>[] classes;
    try {
      classes = getAllClasses(classLoader);
    } catch (IOException | ClassNotFoundException e) {
      throw new AstConvertorException("找不到任何函数.", e);
    }
    List<String> iterateFunctionName = new ArrayList<>();
    // 遍历所有类
    for (Class<?> clazz : classes) {
      // 检查类是否是指定function接口的子类
      if (Function.class.isAssignableFrom(clazz)) {
        // 检查类上是否存在指定的注解
        if (clazz.isAnnotationPresent(FunctionName.class)) {
          // 获取类上的注解实例
          FunctionName functionName = clazz.getAnnotation(FunctionName.class);
          iterateFunctionName.add(functionName.name());
          if (functionName.name().equalsIgnoreCase(operatorName)) {
            try {
              return (Function) clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                     | NoSuchMethodException e) {
              log.warn("无法初始化函数[{}].", operatorName);
              throw new AstConvertorException("无法初始化函数[%s].", operatorName, e);
            }
          }
        }
      }
    }
    throw new AstConvertorException("找不到函数[%s]，可以在下面的列表中查找.\n %s", operatorName,
            iterateFunctionName);
  }

  /**
   * 获取这个classLoader下所有类.
   * 由AI生成.
   */
  private static Class<?>[] getAllClasses(ClassLoader classLoader) throws IOException,
          ClassNotFoundException {
    List<Class<?>> classes = new ArrayList<>();

    // 获取类路径下的所有资源
    Enumeration<URL> resources = classLoader.getResources("");

    // 遍历所有资源
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      File file = new File(resource.getFile());

      // 如果资源是目录，则递归获取所有类文件
      if (file.isDirectory()) {
        classes.addAll(getAllClasses(classLoader, file, ""));
      }
    }

    return classes.toArray(new Class<?>[0]);
  }

  /**
   * 由AI生成.
   */
  private static List<Class<?>> getAllClasses(ClassLoader classLoader, File directory,
                                              String packageName) throws ClassNotFoundException {
    List<Class<?>> classes = new ArrayList<>();

    // 获取目录下的所有文件
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          // 如果是子目录，则递归调用获取所有类文件
          String subPackageName = StringUtils.isEmpty(packageName) ? file.getName() :
                  packageName + "." + file.getName();
          classes.addAll(getAllClasses(classLoader, file, subPackageName));
        } else if (file.getName().endsWith(".class")) {
          // 如果是类文件，则加载类
          String className = packageName + "." + file.getName().replace(".class", "");
          Class<?> clazz = classLoader.loadClass(className);
          classes.add(clazz);
        }
      }
    }

    return classes;
  }

  /**
   * 找到输入列表中是聚合函数的列，返回key为是否都是聚合函数，value是不是聚合函数的列表.
   */
  public static Pair<Boolean, List<RelalgExpr>> allAggFunctions(List<RelalgExpr> relalgExprList) {
    // TODO： 这里要考虑table函数.
    List<RelalgExpr> scalarExprList = new ArrayList<>();
    boolean allAgg = true;
    for (RelalgExpr relalgExpr : relalgExprList) {
      if (FunctionUtils.isAggFunction(relalgExpr)) {
        continue;
      }
      allAgg = false;
      scalarExprList.add(relalgExpr);
    }
    return Pair.of(allAgg, scalarExprList);
  }
}
