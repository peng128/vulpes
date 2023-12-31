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

/**
 * From server to client after command, if no error and result set -- that is,
 * if the command was a query which returned a result set. The Result Set Header
 * Packet is the first of several, possibly many, packets that the server sends
 * for result sets. The order of packets for a result set is:
 * (Result Set Header Packet)   the number of columns
 * (Field Packets)              column descriptors
 * (EOF Packet)                 marker: end of Field Packets
 * (Row Data Packets)           row contents
 * (EOF Packet)                 marker: end of Data Packets
 * Bytes                        Name
 * -----                        ----
 * 1-9   (Length-Coded-Binary)  field_count
 * 1-9   (Length-Coded-Binary)  extra
 *
 *
 * @author mycat
 */
public class ResultSetHeaderPacket {

  public int fieldCount;
  public long extra;
  public byte packetId;

  /**
   * TODO 待重构.
   */
  public void write(ByteBuffer buffer) {
    int size = calcPacketSize();
    BufferUtil.writeUb3(buffer, size);
    buffer.put(packetId);
    BufferUtil.writeLength(buffer, fieldCount);
    if (extra > 0) {
      BufferUtil.writeLength(buffer, extra);
    }
  }

  /**
   * Copy from Mycat.
   */
  public ByteBuffer write() {
    int size = calcPacketSize();
    ByteBuffer buffer = ByteBuffer.allocate(size + 4);
    BufferUtil.writeUb3(buffer, size);
    buffer.put(packetId);
    BufferUtil.writeLength(buffer, fieldCount);
    if (extra > 0) {
      BufferUtil.writeLength(buffer, extra);
    }
    return buffer;
  }

  /**
   * TODO 待重构.
   */
  public int calcPacketSize() {
    int size = BufferUtil.getLength(fieldCount);
    if (extra > 0) {
      size += BufferUtil.getLength(extra);
    }
    return size;
  }

}