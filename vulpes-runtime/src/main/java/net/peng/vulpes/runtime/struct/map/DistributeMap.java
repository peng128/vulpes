package net.peng.vulpes.runtime.struct.map;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description of TemporaryMapStorage.
 * 用于存储kv的存储
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/6
 */
public interface DistributeMap<K, V> {

  boolean contains(K key);

  V get(K key);

  void put(K key, V value);

  Iterator<List<Map.Entry<K, V>>> fetchAll();
}
