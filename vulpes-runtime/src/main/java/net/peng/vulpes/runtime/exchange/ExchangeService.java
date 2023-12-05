package net.peng.vulpes.runtime.exchange;

import lombok.Getter;
import net.peng.vulpes.runtime.DataFetcher;
import net.peng.vulpes.runtime.struct.data.Segment;

/**
 * Description of ExchangeService.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/16
 */
public abstract class ExchangeService implements DataFetcher {

  @Getter
  protected Segment<?> segment;

  public abstract void put(Segment<?> segment);
}
