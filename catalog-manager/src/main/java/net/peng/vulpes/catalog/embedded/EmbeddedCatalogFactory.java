package net.peng.vulpes.catalog.embedded;

import java.util.Map;
import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.CatalogFactory;
import net.peng.vulpes.common.exception.TableException;
import net.peng.vulpes.common.spi.SpiType;

/**
 * Description of EmbeddedCatalogFactory.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
@SpiType("embedded-catalog")
public class EmbeddedCatalogFactory implements CatalogFactory {
  private static final String BASE_FILE_PATH = "catalog.embedded.file.path";

  @Override
  public Catalog createCatalog(Map<String, String> configProp) {
    if (!configProp.containsKey(BASE_FILE_PATH)) {
      throw new TableException("[%s] is not set.", BASE_FILE_PATH);
    }
    return new EmbeddedCatalog(configProp.get(BASE_FILE_PATH));
  }
}
