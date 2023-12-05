package net.peng.vulpes.common.session;

import java.util.Map;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.FileHelper;

/**
 * Description of ConfigurationLoaderSessionManagerFactory.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/5
 */
public class ConfigurationLoaderSessionManagerFactory extends SessionManagerFactory {
  private static final String confFileName = "conf/vuples.conf";

  @Override
  public SessionManager create() {
    String filePath = this.getClass().getClassLoader().getResource(confFileName).getFile();
    final Map<Object, Object> map = FileHelper.yamlReader(filePath, Map.class);
    return SessionManager.builder().config(new Config(map)).build();
  }
}
