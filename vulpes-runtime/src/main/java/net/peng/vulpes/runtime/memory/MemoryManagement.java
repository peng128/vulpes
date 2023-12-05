package net.peng.vulpes.runtime.memory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Description of Memory.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
public class MemoryManagement {

  /**
   * 数据内存索引，key是queryId.
   */
  public static final ConcurrentMap<String, MemorySpace> memoryIndex = new ConcurrentHashMap<>();
}
