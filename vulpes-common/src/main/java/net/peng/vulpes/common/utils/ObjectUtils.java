package net.peng.vulpes.common.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.peng.vulpes.common.exception.ClassMissException;

/**
 * 对象工具类.
 */
public class ObjectUtils {

  /**
   * 全部都不为空.
   */
  public static boolean isNotNull(Object... objects) {
    if (objects == null) {
      return false;
    }
    return Arrays.stream(objects).allMatch(Objects::nonNull);
  }

  /**
   * 有一个是空就返回true.
   */
  public static boolean isAnyNull(Object... objects) {
    return Arrays.stream(objects).anyMatch(Objects::nonNull);
  }

  public static boolean isNull(Object object) {
    return object == null;
  }

  /**
   * 判断对象是否为空.
   *
   * @param object 传入对象
   * @return 是否为空
   */
  public static boolean isEmpty(Object object) {
    if (object == null) {
      return true;
    }
    if (object instanceof CharSequence) {
      return ((CharSequence) object).length() == 0;
    }
    if (object.getClass().isArray()) {
      return Array.getLength(object) == 0;
    }
    if (object instanceof Collection<?>) {
      return ((Collection<?>) object).isEmpty();
    }
    if (object instanceof Map<?, ?>) {
      return ((Map<?, ?>) object).isEmpty();
    }
    return false;
  }

  /**
   * 检查对象是否是这个类型，如果不是就报错，如果是就转换为这个类型.
   */
  public static <T, R> R checkClass(T input, Class<R> clazz,
                                    Class<? extends RuntimeException> exceptionClass) {
    if (clazz.isInstance(input)) {
      return clazz.cast(input);
    }
    try {
      throw exceptionClass.getDeclaredConstructor(String.class).newInstance(String.format("[%s] "
              + "can't cast to class[%s].", input, clazz));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException
             | NoSuchMethodException e) {
      throw new RuntimeException("check class error.", e);
    }
  }

  /**
   * 使用反射的方式来新建对象.
   */
  public static <R> R reflectionNewInstance(String className, Class<R> clazz,
                                            List<Class<?>> parameterTypes, Object... input) {
    try {
      Class<?> newClazz = Class.forName(className);
      return clazz.cast(newClazz.getDeclaredConstructor(parameterTypes.toArray(new Class<?>[0]))
              .newInstance(input));
    } catch (ClassMissException | InstantiationException | IllegalAccessException
             | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
      throw new ClassMissException("找不到配置中的类 %s", e, className);
    }
  }

  /**
   * 找到一个类中，对应类型的静态不可变变量.
   */
  public static <R> List<R> findStaticFinalObject(Class<?> clazz, Class<R> clazzR) {
    List<R> objcetList = new ArrayList<>();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
        try {
          Object object = field.get(null);
          if (clazzR.isInstance(object)) {
            objcetList.add(clazzR.cast(object));
          }
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return objcetList;
  }

  /**
   * 检查输入类是否是某个类的子类.
   * 由AI生成.
   */
  public static boolean isSubclassOf(Class<?> clazz, Class<?> parentClass) {
    // 检查类是否为null
    if (clazz == null) {
      return false;
    }
    // 获取类的父类
    Class<?> superClass = clazz.getSuperclass();
    // 递归检查类的父类是否与指定的父类相同
    if (superClass != null && superClass.equals(parentClass)) {
      return true;
    } else {
      return isSubclassOf(superClass, parentClass);
    }
  }

  /**
   * 判断第一个类集合是否全为第二个类集合的子类.
   */
  public static boolean allSubClass(Class<?>[] childClass, Class<?>[] parentClass) {
    if (isEmpty(childClass) && isEmpty(parentClass)) {
      return true;
    }
    if (childClass.length != parentClass.length) {
      return false;
    }
    for (int i = 0; i < childClass.length; i++) {
      if (!parentClass[i].isAssignableFrom(childClass[i])) {
        return false;
      }
    }
    return true;
  }
}
