package net.peng.vulpes.catalog;

import java.util.List;
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

  /**
   * 获取对应库下所有的表.
   */
  List<String> getTableNames(String schema);

  /**
   * 获取这个目录下所有的库.
   */
  List<String> getSchemas();
}
