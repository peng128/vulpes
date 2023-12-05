package net.peng.vulpes.catalog.loader;

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
}
