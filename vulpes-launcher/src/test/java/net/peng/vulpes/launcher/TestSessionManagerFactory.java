package net.peng.vulpes.launcher;

import java.util.Properties;
import java.util.function.Function;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.session.SessionManagerFactory;

/**
 * Description of TestSessionManagerFactory.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
public class TestSessionManagerFactory extends SessionManagerFactory {
  @Override
  public SessionManager create() {
    return SessionManager.builder().currentCatalog("embedded-catalog").currentSchema("test")
            .config(buildConfig()).build();
  }

  protected Config buildConfig() {
    Properties properties = new Properties();
    properties.put(ConfigItems.CATALOG_STATIC_CONFIG_FILE_PATH.name(),
            TestSessionManagerFactory.class.getClassLoader()
                    .getResource("catalog-embedded.yaml").getFile());
    properties.put(ConfigItems.CATALOG_LOADER_CLASS.name(),
            "net.peng.vulpes.catalog.loader.StaticCatalogLoader");
    Function<String, String> path =
            input -> "file://" + TestSessionManagerFactory.class.getClassLoader()
                    .getResource(input).getFile();
    properties.put(ConfigItems.FILE_PATH_PROCESS_WRAPPER.name(), path);
    return new Config(properties);
  }
}
