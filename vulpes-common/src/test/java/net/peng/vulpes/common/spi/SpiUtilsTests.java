package net.peng.vulpes.common.spi;

import org.junit.Assert;
import org.junit.Test;

/**
 * Description of SpiUtilsTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
public class SpiUtilsTests {

  @Test
  public void spiLoader() {
    Assert.assertEquals("test1", SpiUtils.spiLoader(TestSpi.class, "test1").getTestValue());
    Assert.assertEquals("test2", SpiUtils.spiLoader(TestSpi.class, "test2").getTestValue());
  }
}
