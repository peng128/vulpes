package net.peng.vulpes.runtime.struct.data;

/**
 * Description of Segment.
 * 用于存储数据的接口.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/10
 */
public interface Segment<T> {

  T get();

  void put(T t);
}
