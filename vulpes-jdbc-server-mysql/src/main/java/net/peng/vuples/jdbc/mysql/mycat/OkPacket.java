/*
 * Copyright (c) 2020, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */

package net.peng.vuples.jdbc.mysql.mycat;

import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * From server to client in response to command, if no error and no result set.
 * Bytes                       Name
 * -----                       ----
 * 1                           field_count, always = 0
 * 1-9 (Length Coded Binary)   affected_rows
 * 1-9 (Length Coded Binary)   insert_id
 * 2                           server_status
 * 2                           warning_count
 * n   (until end of packet)   message fix:(Length Coded String)
 *
 * @author mycat
 */
@AllArgsConstructor
@Builder
public class OkPacket {
  public static final byte FIELD_COUNT = 0x00;
  public static final byte[] OK = new byte[]{7, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0};

  @Builder.Default
  public long affectedRows = 0;
  @Builder.Default
  public long insertId = 0;
  @Builder.Default
  public int serverStatus = 2;
  @Builder.Default
  public int warningCount = 0;
  @Builder.Default
  public byte[] message = "".getBytes();
  public byte packetId;

  /**
   * Copy from Mycat.
   */
  public ByteBuffer write() {
    int size = calcPacketSize();
    ByteBuffer buffer = ByteBuffer.allocate(size + 4);
    BufferUtil.writeUb3(buffer, calcPacketSize());
    buffer.put(packetId);
    buffer.put(FIELD_COUNT);
    BufferUtil.writeLength(buffer, affectedRows);
    BufferUtil.writeLength(buffer, insertId);
    BufferUtil.writeUb2(buffer, serverStatus);
    BufferUtil.writeUb2(buffer, warningCount);
    if (message != null) {
      BufferUtil.writeWithLength(buffer, message);
    }
    return buffer;

  }

  /**
   * Copy from Mycat.
   */
  public int calcPacketSize() {
    int i = 1;
    i += BufferUtil.getLength(affectedRows);
    i += BufferUtil.getLength(insertId);
    i += 4;
    if (message != null) {
      i += BufferUtil.getLength(message);
    }
    return i;
  }

}