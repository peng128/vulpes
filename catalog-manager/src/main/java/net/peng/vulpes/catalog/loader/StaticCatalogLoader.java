package net.peng.vulpes.catalog.loader;

import java.util.HashMap;
import java.util.Map;
import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.CatalogFactory;
import net.peng.vulpes.catalog.loader.config.CatalogConfiguration;
import net.peng.vulpes.catalog.loader.config.CatalogItem;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.configuration.FileHelper;
import net.peng.vulpes.common.exception.TableException;
import net.peng.vulpes.common.spi.SpiUtils;

/**
 * Description of EmbeddedCatalogLoader.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public class StaticCatalogLoader implements CatalogLoader {

  private final Map<String, Catalog> catalogs;

  /**
   * 使用配置文件初始化catalog加载器.
   */
  public StaticCatalogLoader(Config config) {
    Map<String, Catalog> catalogMap = new HashMap<>();
    CatalogConfiguration catalogConfiguration =
            FileHelper.yamlReader(config.get(ConfigItems.CATALOG_STATIC_CONFIG_FILE_PATH),
                    CatalogConfiguration.class);
    for (CatalogItem catalog : catalogConfiguration.catalogs()) {
      CatalogFactory catalogFactory = SpiUtils.spiLoader(CatalogFactory.class, catalog.type());
      catalogMap.put(catalog.name(), catalogFactory.createCatalog(catalog.prop()));
    }
    this.catalogs = catalogMap;
  }

  @Override
  public Catalog getCatalog(String catalog) {
    if (!catalogs.containsKey(catalog)) {
      throw new TableException("找不到目录[%s], 所有目录为[%s]", catalog, catalogs.keySet());
    }
    return catalogs.get(catalog);
  }
}
