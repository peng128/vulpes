package net.peng.vulpes.common.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.exception.PluginsLoaderException;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * Description of SpiUtils.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
@Slf4j
public class SpiUtils {

  /**
   * 通过SPI找到这个类下面所有的实现. (需要注册到SPI中)
   */
  public static <T> List<T> spiLoader(Class<T> className) {
    ServiceLoader<T> s = ServiceLoader.load(className);
    Iterator<T> iterator = s.iterator();
    List<T> objects = new ArrayList<>();
    while (iterator.hasNext()) {
      T findClass = iterator.next();
      objects.add(findClass);
    }
    return objects;
  }

  /**
   * 通过SPI找到这个类下面所有的实现. (需要注册到SPI中)
   */
  public static <T> T spiLoader(Class<T> className, String spiType) {
    ServiceLoader<T> s = ServiceLoader.load(className);
    Iterator<T> iterator = s.iterator();
    List<T> objects = new ArrayList<>();
    List<T> allObjects = new ArrayList<>();
    while (iterator.hasNext()) {
      T findClass = iterator.next();
      allObjects.add(findClass);
      SpiType type = findClass.getClass().getAnnotation(SpiType.class);
      if (ObjectUtils.isNotNull(type) && ObjectUtils.isNotNull(type.value())
              && type.value().equalsIgnoreCase(spiType)) {
        objects.add(findClass);
      }
    }
    // 如果找到两个以上的对象，就报个错.
    if (objects.size() > 1) {
      throw new PluginsLoaderException("找到了多于一个[%s]的实现，检查一下代码吧.\n%s", spiType,
              spiTypeFullName(objects));
    }
    // 没有找到实现也报错.
    if (objects.size() == 0) {
      throw new PluginsLoaderException("没有找到[%s]的实现，检查一下代码吧.所有的类型是:\n%s", spiType,
              spiTypeFullName(allObjects));
    }
    log.info("通过spi加载: {}", spiTypeFullName(objects.get(0)));
    return objects.get(0);
  }

  /**
   * 这个类型的全名，并打印出spiType的内容.
   */
  public static <T> String spiTypeFullName(T... objects) {
    StringBuilder stringBuilder = new StringBuilder();
    for (T object : objects) {
      SpiType type = object.getClass().getAnnotation(SpiType.class);
      stringBuilder.append("class: [").append(object.getClass().getName()).append("];")
              .append(" spi type: [");
      if (ObjectUtils.isNotNull(type) && ObjectUtils.isNotNull(type.value())) {
        stringBuilder.append(type.value());
      }
      stringBuilder.append("].\n");
    }
    return stringBuilder.toString();
  }
}
