package net.peng.vulpes.common.spi;

/**
 * Description of TestTwoSpiImpl.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
@SpiType("test2")
public class TestTwoSpiImpl implements TestSpi {
  @Override
  public String getTestValue() {
    return "test2";
  }
}
