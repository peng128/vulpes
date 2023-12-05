package net.peng.vulpes.launcher.utils;

import java.nio.charset.Charset;
import net.peng.vuples.jdbc.mysql.mycat.IntegerUtil;
import net.peng.vuples.jdbc.mysql.mycat.LongUtil;

/**
 * Description of ByteUtils.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
public class ByteUtils {

  /**
   * 将基本类型转为字节.
   */
  public static byte[] getBytes(Object object) {
    if (object instanceof Long data) {
      return LongUtil.toBytes(data);
    } else if (object instanceof Integer data) {
      return IntegerUtil.toBytes(data);
    } else if (object instanceof String data) {
      return data.getBytes(Charset.defaultCharset());
    } else {
      return object.toString().getBytes();
    }
  }
}
