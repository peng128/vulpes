package net.peng.vulpes.common.configuration;

/**
 * Description of ConfigObject.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/16
 */
public record ConfigObject<T>(String name, T defaultValue) {

  public static <T> ConfigObject<T> create(String name) {
    return new ConfigObject<>(name, null);
  }

  public static <T> ConfigObject<T> create(String name, T defaultValue) {
    return new ConfigObject<>(name, defaultValue);
  }
}
