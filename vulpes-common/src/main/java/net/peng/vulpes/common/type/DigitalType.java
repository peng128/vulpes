package net.peng.vulpes.common.type;

/**
 * Description of DigitalType.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/9
 */
public abstract class DigitalType extends DataType {

  protected DigitalType(int precision, int scale, boolean nullable) {
    super(precision, scale, nullable);
  }
}
