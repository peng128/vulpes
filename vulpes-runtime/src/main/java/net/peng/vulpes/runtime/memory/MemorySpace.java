package net.peng.vulpes.runtime.memory;

import lombok.Builder;
import lombok.Getter;
import org.apache.arrow.memory.BufferAllocator;

/**
 * Description of MemorySpace.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
@Builder
@Getter
public class MemorySpace implements AutoCloseable {

  private final BufferAllocator allocator;

  @Override
  public void close() {
    allocator.close();
  }
}
