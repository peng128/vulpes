package net.peng.vulpes.catalog;

import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.model.TableIdentifier;

/**
 * Description of Catalog.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public interface Catalog {

  /**
   * 获取表元数据.
   */
  TableMeta getTable(TableIdentifier tableIdentifier);
}
