package net.peng.vuples.jdbc.mysql.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Description of JdbcServer.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/16
 */
public class JdbcServer {

  public static final byte PROTOCOL_VERSION = 10;

  private static final byte[] FILLER_13 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

  public static byte[] SERVER_VERSION = "8.0.23".getBytes();

  private static final byte[] FILLER_10 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

  public static final String DEFAULT_AUTH_PLUGIN_NAME_STRING = "mysql_native_password";

  public static final byte[] DEFAULT_AUTH_PLUGIN_NAME = "mysql_native_password".getBytes();

  public static byte[] AUTH_OK = new byte[]{7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0};

  public static final byte STATUS = (byte) 0XFE;

  public static final byte REQUEST_TYPE_CMD = 3;

  public static final byte REQUEST_TYPE_QUIT = 1;

  /**
   * TODO 需要重构.
   */
  public static HeaderInfo write() throws IOException {
    final byte[] rand1 = RandomUtil.randomBytes(8);
    final byte[] rand2 = RandomUtil.randomBytes(12);
    final int serverCapabilities = getServerCapabilities();
    final byte[] restOfScrambleBuff = rand2;
    int size = calcPacketSize(serverCapabilities, restOfScrambleBuff);
    // 加文件头的4个字节.
    ByteBuffer buffer = ByteBuffer.allocate(size + 4);
    writeUb3(buffer, calcPacketSize(serverCapabilities, restOfScrambleBuff));
    final Byte packetId = 0;
    buffer.put(packetId);
    buffer.put(PROTOCOL_VERSION);
    writeWithNull(buffer, SERVER_VERSION);
    writeUb4(buffer, 10);
    writeWithNull(buffer, rand1);
    writeUb2(buffer, serverCapabilities);
    buffer.put((byte) (0 & 0xff));
    writeUb2(buffer, 2);
    writeUb2(buffer, (serverCapabilities >> 16)); // capability flags (upper 2 bytes)
    final byte[] seed1 = rand1;
    if ((serverCapabilities & Capabilities.CLIENT_PLUGIN_AUTH) != 0) {
      if (restOfScrambleBuff.length <= 13) {
        buffer.put((byte) (seed1.length + 13));
      } else {
        buffer.put((byte) (seed1.length + restOfScrambleBuff.length));
      }
    } else {
      buffer.put((byte) 0);
    }
    buffer.put(FILLER_10);
    if ((serverCapabilities & Capabilities.CLIENT_SECURE_CONNECTION) != 0) {
      buffer.put(restOfScrambleBuff);
      // restOfScrambleBuff.length always to be 12
      if (restOfScrambleBuff.length < 13) {
        for (int i = 13 - restOfScrambleBuff.length; i > 0; i--) {
          buffer.put((byte) 0);
        }
      }
    }
    if ((serverCapabilities & Capabilities.CLIENT_PLUGIN_AUTH) != 0) {
      writeWithNull(buffer, DEFAULT_AUTH_PLUGIN_NAME);
    }
    // 保存认证数据
    byte[] seed = new byte[rand1.length + rand2.length];
    System.arraycopy(rand1, 0, seed, 0, rand1.length);
    System.arraycopy(rand2, 0, seed, rand1.length, rand2.length);

    return new HeaderInfo(buffer, seed);
  }

  /**
   * Copy from Mycat.
   */
  public static final void writeUb2(ByteBuffer buffer, int i) {
    buffer.put((byte) (i & 0xff));
    buffer.put((byte) (i >>> 8));
  }

  /**
   * Copy from Mycat.
   */
  public static final void writeUb3(ByteBuffer buffer, int i) {
    buffer.put((byte) (i & 0xff));
    buffer.put((byte) (i >>> 8));
    buffer.put((byte) (i >>> 16));
  }

  /**
   * Copy from Mycat.
   */
  public static final void writeUb4(ByteBuffer buffer, long l) {
    buffer.put((byte) (l & 0xff));
    buffer.put((byte) (l >>> 8));
    buffer.put((byte) (l >>> 16));
    buffer.put((byte) (l >>> 24));
  }

  /**
   * Copy from Mycat.
   */
  public static final void writeWithNull(ByteBuffer buffer, byte[] src) {
    buffer.put(src);
    buffer.put((byte) 0);
  }

  protected static int getServerCapabilities() {
    int flag = 0;
    flag |= Capabilities.CLIENT_LONG_PASSWORD;
    flag |= Capabilities.CLIENT_FOUND_ROWS;
    flag |= Capabilities.CLIENT_LONG_FLAG;
    boolean usingCompress = false;
    if (usingCompress) {
      flag |= Capabilities.CLIENT_COMPRESS;
    }
    flag |= Capabilities.CLIENT_IGNORE_SPACE;
    flag |= Capabilities.CLIENT_PROTOCOL_41;
    flag |= Capabilities.CLIENT_INTERACTIVE;
    flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
    flag |= Capabilities.CLIENT_SECURE_CONNECTION;
    boolean useHandshakeV10 = true;
    if (useHandshakeV10) {
      flag |= Capabilities.CLIENT_PLUGIN_AUTH;
    }
    return flag;
  }

  /**
   * Copy from Mycat.
   */
  public static int calcPacketSize(int serverCapabilities, byte[] restOfScrambleBuff) {
    int size = 1; // protocol version
    size += (SERVER_VERSION.length + 1); // server version
    size += 4; // connection id
    size += 8;
    size += 1; // [00] filler
    size += 2; // capability flags (lower 2 bytes)
    size += 1; // character set
    size += 2; // status flags
    size += 2; // capability flags (upper 2 bytes)
    size += 1;
    size += 10; // reserved (all [00])
    if ((serverCapabilities & Capabilities.CLIENT_SECURE_CONNECTION) != 0) {
      // restOfScrambleBuff.length always to be 12
      if (restOfScrambleBuff.length <= 13) {
        size += 13;
      } else {
        size += restOfScrambleBuff.length;
      }
    }
    if ((serverCapabilities & Capabilities.CLIENT_PLUGIN_AUTH) != 0) {
      size += (DEFAULT_AUTH_PLUGIN_NAME.length + 1); // auth-plugin name
    }
    return size;
  }

  @SuppressWarnings("checkstyle:MissingJavadocType")
  @Data
  @AllArgsConstructor
  public static class HeaderInfo {
    final ByteBuffer header;
    final byte[] seed;
  }
}
