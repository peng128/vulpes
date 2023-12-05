package net.peng.vuples.jdbc.mysql;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.peng.vuples.jdbc.mysql.mycat.EofPacket;
import net.peng.vuples.jdbc.mysql.mycat.FieldPacket;
import net.peng.vuples.jdbc.mysql.mycat.OkPacket;
import net.peng.vuples.jdbc.mysql.mycat.PacketUtil;
import net.peng.vuples.jdbc.mysql.mycat.ResultSetHeaderPacket;
import net.peng.vuples.jdbc.mysql.mycat.RowDataPacket;
import net.peng.vuples.jdbc.mysql.socket.JdbcServer;

/**
 * Description of ResultSetWrapper.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
public class ResultSetBuilder {
  private final List<String> fieldName;
  private final List<Integer> fieldTypes;
  private final List<List<byte[]>> filedData;

  /**
   * 返回数据包构建器.
   */
  public ResultSetBuilder(List<String> fieldName, List<Integer> fieldTypes,
                          List<List<byte[]>> filedData) {
    this.fieldName = fieldName;
    this.fieldTypes = fieldTypes;
    this.filedData = filedData;
  }

  /**
   * 编码.
   */
  public ByteBuffer encode() {
    final List<ByteBuffer> buffers = new ArrayList<>();
    ResultSetHeaderPacket header = PacketUtil.getHeader(fieldName.size());
    FieldPacket[] fields = new FieldPacket[fieldName.size()];
    EofPacket eof = new EofPacket();
    byte packetId = 0;
    header.packetId = ++packetId;
    for (int i = 0; i < fieldName.size(); i++) {
      fields[i] = PacketUtil.getField(fieldName.get(i), fieldTypes.get(i));
      fields[i].packetId = ++packetId;
    }
    eof.packetId = ++packetId;
    // Write header
    buffers.add(header.write());
    for (FieldPacket field : fields) {
      buffers.add(field.write());
    }
    buffers.add(eof.write());
    //Write data
    for (List<byte[]> dataList : filedData) {
      RowDataPacket row = new RowDataPacket(fieldName.size());
      for (byte[] bytes : dataList) {
        row.add(bytes);
      }
      row.packetId = ++packetId;
      buffers.add(row.write());
    }
    //End
    EofPacket lastEof = new EofPacket();
    lastEof.packetId = ++packetId;
    buffers.add(lastEof.write());
    int totalLength = 0;
    for (ByteBuffer byteBuffer : buffers) {
      totalLength += byteBuffer.position();
    }
    ByteBuffer result = ByteBuffer.allocate(totalLength);
    for (ByteBuffer byteBuffer : buffers) {
      result.put(byteBuffer.array());
    }
    return result;
  }

}
