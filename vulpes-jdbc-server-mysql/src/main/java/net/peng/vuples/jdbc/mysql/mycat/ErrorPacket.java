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
 * From server to client in response to command, if error.
 * Bytes                       Name
 * -----                       ----
 * 1                           field_count, always = 0xff
 * 2                           errno
 * 1                           (sqlstate marker), always '#'
 * 5                           sqlstate (5 characters)
 * n                           message
 *
 * @author mycat
 */
@AllArgsConstructor
@Builder
public class ErrorPacket {
  public static final byte FIELD_COUNT = (byte) 0xff;
  private static final byte SQLSTATE_MARKER = (byte) '#';
  private static final byte[] DEFAULT_SQLSTATE = "HY000".getBytes();

  public int errno;
  @Builder.Default
  public byte mark = SQLSTATE_MARKER;
  @Builder.Default
  public byte[] sqlState = DEFAULT_SQLSTATE;
  public byte[] message;
  @Builder.Default
  public byte packetId = 1;

  /**
   * Copy from Mycat.
   */
  public ByteBuffer writeToBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(calcPacketSize() + 4);
    int size = calcPacketSize();
    BufferUtil.writeUb3(buffer, size);
    buffer.put(packetId);
    buffer.put(FIELD_COUNT);
    BufferUtil.writeUb2(buffer, errno);
    buffer.put(mark);
    buffer.put(sqlState);
    if (message != null) {
      buffer.put(message);
    }
    return buffer;
  }

  /**
   * Copy from Mycat.
   */
  public int calcPacketSize() {
    int size = 9; // 1 + 2 + 1 + 5
    if (message != null) {
      size += message.length;
    }
    return size;
  }
}