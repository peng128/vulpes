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
 * From Server To Client, at the end of a series of Field Packets, and at the
 * end of a series of Data Packets.With prepared statements, EOF Packet can also
 * end parameter information, which we'll describe later.
 * Bytes                 Name
 * -----                 ----
 * 1                     field_count, always = 0xfe
 * 2                     warning_count
 * 2                     Status Flags
 *
 *
 * @author mycat
 */
public class EofPacket {
  public static final byte FIELD_COUNT = (byte) 0xfe;

  public byte fieldCount = FIELD_COUNT;
  public int warningCount;
  public int status = 2;
  public byte packetId;

  public int calcPacketSize() {
    return 5; // 1+2+2;
  }

  /**
   * Copy from Mycat.
   */
  public void write(ByteBuffer buffer) {
    int size = calcPacketSize();
    BufferUtil.writeUb3(buffer, size);
    buffer.put(packetId);
    buffer.put(fieldCount);
    BufferUtil.writeUb2(buffer, warningCount);
    BufferUtil.writeUb2(buffer, status);
  }

  /**
   * Copy from Mycat.
   */
  public ByteBuffer write() {
    int size = calcPacketSize();
    ByteBuffer buffer = ByteBuffer.allocate(size + 4);
    BufferUtil.writeUb3(buffer, size);
    buffer.put(packetId);
    buffer.put(fieldCount);
    BufferUtil.writeUb2(buffer, warningCount);
    BufferUtil.writeUb2(buffer, status);
    return buffer;
  }

}