package net.peng.vulpes.common.session;

import org.junit.Assert;
import org.junit.Test;

/**
 * Description of ConfigurationLoaderSessionFactoryTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/5
 */
public class ConfigurationLoaderSessionFactoryTests {

  /**
   * 测试数据为:
   * test_conf_key1: test_conf_value1
   * test_conf_key2: test_conf_value2.
   */
  @Test
  public void createTest() {
    ConfigurationLoaderSessionManagerFactory configurationLoaderSessionManagerFactory =
            new ConfigurationLoaderSessionManagerFactory();
    SessionManager sessionManager = configurationLoaderSessionManagerFactory.create();
    Assert.assertEquals("test_conf_value1",
            sessionManager.getConfig().get("test_conf_key1"));
    Assert.assertEquals("test_conf_value2",
            sessionManager.getConfig().get("test_conf_key2"));
  }
}
