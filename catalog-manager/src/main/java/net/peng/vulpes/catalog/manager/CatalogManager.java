package net.peng.vulpes.catalog.manager;

import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.loader.CatalogLoader;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.exception.TableException;
import net.peng.vulpes.common.model.TableIdentifier;

/**
 * Description of CatalogManager.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public class CatalogManager {

  private final CatalogLoader catalogLoader;

  public CatalogManager(CatalogLoader catalogLoader) {
    this.catalogLoader = catalogLoader;
  }

  public Catalog getCatalog(String catalog) {
    return catalogLoader.getCatalog(catalog);
  }

  /**
   * 根据表标识符，或者表元数据.
   */
  public TableMeta getTable(TableIdentifier tableIdentifier) {
    if (!tableIdentifier.fullTableName()) {
      throw new TableException("需要全路径表标识符[catalog.schema.table]. [%s]", tableIdentifier);
    }
    final Catalog catalog = getCatalog(tableIdentifier.getCatalog());
    return catalog.getTable(tableIdentifier);
  }
}
