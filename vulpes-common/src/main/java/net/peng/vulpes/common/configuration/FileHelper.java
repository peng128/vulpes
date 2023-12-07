package net.peng.vulpes.common.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.exception.ConfigMissingException;

/**
 * Description of FileHelper.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
@Slf4j
public class FileHelper {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  /**
   * 从传入目录读取YAML配置文件.
   */
  public static <R> R yamlReader(String filePath, Class<R> clazz) {
    try {
      // 创建 ObjectMapper 对象，并使用 YAMLFactory 初始化
      final File file = new File(filePath);
      R yaml = OBJECT_MAPPER.readValue(file, clazz);
      log.info("从[{}]读取到配置[{}]:\n{}", filePath, clazz.getName(), yaml);
      return yaml;
    } catch (IOException e) {
      throw new ConfigMissingException("找不到这个文件[%s],或转换这个类型报错[%s]", e, filePath, clazz.getName());
    }
  }

  /**
   * 获取子目录名称列表.
   */
  public static List<String> listDirectoryNames(String path) {
    // 创建 ObjectMapper 对象，并使用 YAMLFactory 初始化
    final File file = new File(path);
    return Arrays.stream(file.listFiles()).filter(File::isDirectory).map(File::getName).toList();
  }

  /**
   * 获取目录下元素列表.
   */
  public static List<String> listSubNames(String path) {
    // 创建 ObjectMapper 对象，并使用 YAMLFactory 初始化
    final File file = new File(path);
    return Arrays.stream(file.listFiles()).map(File::getName).toList();
  }
}
