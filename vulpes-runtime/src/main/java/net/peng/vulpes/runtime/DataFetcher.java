package net.peng.vulpes.runtime;

import java.util.List;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.struct.data.Segment;
import org.apache.arrow.dataset.file.FileFormat;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;

/**
 * Description of DataFetcher.
 * 用于获取数据的接口.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
public interface DataFetcher {

  /**
   * 获取数据接口.
   */
  Segment<?> fetch(MemorySpace memorySpace);
}
