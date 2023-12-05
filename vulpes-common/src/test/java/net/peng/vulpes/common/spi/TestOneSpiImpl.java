package net.peng.vulpes.common.spi;

/**
 * Description of TestSpiImpl.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
@SpiType("test1")
public class TestOneSpiImpl implements TestSpi {
  @Override
  public String getTestValue() {
    return "test1";
  }
}
