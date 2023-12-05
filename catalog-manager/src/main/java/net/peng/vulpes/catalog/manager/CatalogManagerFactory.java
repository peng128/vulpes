package net.peng.vulpes.catalog.manager;

import java.util.List;
import net.peng.vulpes.catalog.loader.CatalogLoader;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * Description of CatalogManagerFactory.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public class CatalogManagerFactory {

  /**
   * 根据配置新建{@link CatalogManager}.
   */
  public static CatalogManager newInstance(Config config) {
    String className = config.get(ConfigItems.CATALOG_LOADER_CLASS);
    CatalogLoader loader = ObjectUtils.reflectionNewInstance(className, CatalogLoader.class,
            List.of(Config.class), config);
    return new CatalogManager(loader);
  }
}
