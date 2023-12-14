package net.peng.vulpes.catalog.file.arrow;

import java.util.Map;
import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.CatalogFactory;
import net.peng.vulpes.common.exception.TableException;
import net.peng.vulpes.common.spi.SpiType;

/**
 * Description of FileParquetCatalogFactory.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/11
 */
@SpiType("file-arrow-catalog")
public class FileArrowCatalogFactory implements CatalogFactory {
  private static final String BASE_FILE_PATH = "catalog.embedded.file.path";

  @Override
  public Catalog createCatalog(Map<String, String> configProp) {
    if (!configProp.containsKey(BASE_FILE_PATH)) {
      throw new TableException("[%s] is not set.", BASE_FILE_PATH);
    }
    return new FileArrowCatalog(configProp.get(BASE_FILE_PATH));
  }
}
