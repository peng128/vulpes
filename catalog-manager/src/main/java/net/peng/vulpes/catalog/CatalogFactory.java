package net.peng.vulpes.catalog;

import java.util.Map;

/**
 * Description of CatalogFactory.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/14
 */
public interface CatalogFactory {

  /**
   * 创建对应{@link Catalog}.
   */
  Catalog createCatalog(Map<String, String> configProp);
}
