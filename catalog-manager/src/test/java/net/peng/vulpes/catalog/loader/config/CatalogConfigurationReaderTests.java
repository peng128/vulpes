package net.peng.vulpes.catalog.loader.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.peng.vulpes.common.configuration.FileHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of CatalogConfigurationReaderTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public class CatalogConfigurationReaderTests {

  @Test
  public void yamlReadTest() {
    final String filePath = CatalogConfigurationReaderTests.class.getClassLoader().getResource(
            "catalog.yaml").getFile();
    Map<String, String> itemProp = new HashMap<>(3);
    itemProp.put("prop1", "value1");
    itemProp.put("prop2", "value2");
    itemProp.put("prop3", "value3");
    CatalogConfiguration except = new CatalogConfiguration(Arrays.asList(new CatalogItem("default",
            "embedded", null), new CatalogItem("test1", "unknown", itemProp)));
    CatalogConfiguration result = FileHelper.yamlReader(filePath, CatalogConfiguration.class);
    Assert.assertEquals(except, result);
  }
}
