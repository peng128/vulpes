package net.peng.vulpes.catalog.loader;

import java.util.List;
import net.peng.vulpes.catalog.Catalog;

/**
 * Description of CatalogLoader.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public interface CatalogLoader {

  Catalog getCatalog(String catalog);

  /**
   * 获取所有目录名称.
   */
  List<String> getCatalogNames();
}
