package net.peng.vulpes.common.configuration;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.ToString;
import net.peng.vulpes.common.exception.ConfigMissingException;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * Description of Config.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
@ToString
public class Config {
  private final Properties properties = new Properties();

  public Config(Properties properties) {
    loadDefaultConfig();
    this.properties.putAll(properties);
  }

  public Config(Map<Object, Object> map) {
    loadDefaultConfig();
    this.properties.putAll(map);
  }

  private void loadDefaultConfig() {
    List<ConfigObject> configs = ObjectUtils.findStaticFinalObject(ConfigItems.class,
            ConfigObject.class);
    for (ConfigObject config : configs) {
      properties.put(config.name(), config.defaultValue());
    }
  }

  /**
   * 获取配置项的值.
   */
  public <T> T get(ConfigObject<T> configObject) {
    Object value = properties.getOrDefault(configObject.name(), configObject.defaultValue());
    if (ObjectUtils.isNull(value)) {
      throw new ConfigMissingException("找不到配置项[%s].", configObject.name());
    }
    return (T) value;
  }

  /**
   * 根据名称获取配置项.
   */
  public Object get(String item) {
    return properties.get(item);
  }

  /**
   * 设置配置项.
   * TODO: 类型这里需要设计一下.
   */
  public void set(String name, Object value) {
    properties.put(name, value);
  }
}
