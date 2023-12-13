package net.peng.vulpes.runtime.struct.map;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Description of MemoryDistributeMap.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/9
 */
public class BucketDistributeMap<K, V> implements DistributeMap<K, V> {

  private final Integer bucketNum;

  private final Map<Integer, Map<K, V>> bucketData;

  /**
   * 初始化分桶map空间.
   */
  public BucketDistributeMap(Integer bucketNum) {
    this.bucketNum = bucketNum;
    this.bucketData = new ConcurrentHashMap<>(bucketNum);
    for (Integer i = 0; i < bucketNum; i++) {
      bucketData.put(i, new ConcurrentHashMap<>());
    }
  }

  @Override
  public boolean contains(K key) {
    return getData(key).containsKey(key);
  }

  @Override
  public V get(K key) {
    return getData(key).get(key);
  }

  @Override
  public void put(K key, V value) {
    getData(key).put(key, value);
  }

  @Override
  public Iterator<List<Map.Entry<K, V>>> fetchAll() {
    return bucketData.values().stream().map(Map::entrySet).map(x -> x.stream().toList()).iterator();
  }

  protected Map<K, V> getData(K key) {
    return bucketData.get(key.hashCode() % bucketNum);
  }
}
