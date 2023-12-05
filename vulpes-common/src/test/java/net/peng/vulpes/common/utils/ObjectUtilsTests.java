package net.peng.vulpes.common.utils;

import java.util.List;
import net.peng.vulpes.common.configuration.ConfigObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of ObjectUtilsTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/27
 */
public class ObjectUtilsTests {
  public static final ConfigObject<String> CATALOG_LOADER_CLASS =
          ConfigObject.create("catalog.loader.class",
                  "net.peng.vulpes.catalog.loader.StaticCatalogLoader");

  @Test
  public void findStaticFinalParameterTest() {
    List<ConfigObject> result = ObjectUtils.findStaticFinalObject(ObjectUtilsTests.class,
            ConfigObject.class);
    Assert.assertEquals(CATALOG_LOADER_CLASS, result.get(0));
  }
}
