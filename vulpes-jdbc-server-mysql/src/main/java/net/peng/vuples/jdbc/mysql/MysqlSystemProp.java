package net.peng.vuples.jdbc.mysql;

import java.nio.ByteBuffer;
import net.peng.vuples.jdbc.mysql.mycat.EofPacket;
import net.peng.vuples.jdbc.mysql.mycat.FieldPacket;
import net.peng.vuples.jdbc.mysql.mycat.Fields;
import net.peng.vuples.jdbc.mysql.mycat.LongUtil;
import net.peng.vuples.jdbc.mysql.mycat.PacketUtil;
import net.peng.vuples.jdbc.mysql.mycat.ResultSetHeaderPacket;
import net.peng.vuples.jdbc.mysql.mycat.RowDataPacket;

/**
 * Description of MysqlSystemProp.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/17
 */
public class MysqlSystemProp {

  private static final int FIELD_COUNT = 21;
  private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
  private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
  private static final EofPacket eof = new EofPacket();

  static {
    int i = 0;
    byte packetId = 0;
    header.packetId = ++packetId;
    fields[i] = PacketUtil.getField("auto_increment_increment", Fields.FIELD_TYPE_LONG);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("character_set_client", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("character_set_connection", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("character_set_results", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("character_set_server", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("collation_server", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("collation_connection", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("init_connect", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("interactive_timeout", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("license", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("lower_case_table_names", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("max_allowed_packet", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("net_write_timeout", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("performance_schema", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("query_cache_size", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("query_cache_type", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("sql_mode", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("system_time_zone", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("time_zone", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("transaction_isolation", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    fields[i] = PacketUtil.getField("wait_timeout", Fields.FIELD_TYPE_VAR_STRING);
    fields[i++].packetId = ++packetId;
    eof.packetId = ++packetId;
  }

  /**
   * Copy from Mycat.
   */
  public static ByteBuffer response(ByteBuffer buffer) {
    header.write(buffer);
    for (FieldPacket field : fields) {
      field.write(buffer);
    }
    eof.write(buffer);
    RowDataPacket row = new RowDataPacket(FIELD_COUNT);
    row.add(LongUtil.toBytes(1));
    row.add("utf8mb4".getBytes());
    row.add("utf8mb4".getBytes());
    row.add("utf8mb4".getBytes());
    row.add("utf8mb4".getBytes());
    row.add("utf8mb4_0900_ai_ci".getBytes());
    row.add("utf8mb4_0900_ai_ci".getBytes());
    row.add("".getBytes());
    row.add("28800".getBytes());
    row.add("GPL".getBytes());
    row.add("0".getBytes());
    row.add("16777216".getBytes());
    row.add("60".getBytes());
    row.add("ON".getBytes());
    row.add("0".getBytes());
    row.add("OFF".getBytes());
    row.add("STRICT_TRANS_TABLES".getBytes());
    row.add("PST".getBytes());
    row.add("SYSTEM".getBytes());
    row.add("REPEATABLE-READ".getBytes());
    row.add("28800".getBytes());
    byte packetId = eof.packetId;
    row.packetId = ++packetId;
    row.write(buffer);
    EofPacket lastEof = new EofPacket();
    lastEof.packetId = ++packetId;
    lastEof.write(buffer);
    return buffer;
  }
}
