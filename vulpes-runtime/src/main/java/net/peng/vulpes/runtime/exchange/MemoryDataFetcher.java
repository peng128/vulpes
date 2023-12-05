package net.peng.vulpes.runtime.exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.peng.vulpes.runtime.DataFetcher;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.Segment;

/**
 * Description of MemoryDataFetcher.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/14
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryDataFetcher extends ExchangeService {

  private Segment<?> segment;

  @Override
  public Segment<?> fetch(MemorySpace memorySpace) {
    return segment;
  }

  @Override
  public void put(Segment<?> segment) {
    this.segment = segment;
  }
}
