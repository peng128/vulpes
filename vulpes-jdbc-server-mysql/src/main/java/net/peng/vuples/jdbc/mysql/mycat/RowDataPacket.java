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
import java.util.ArrayList;
import java.util.List;

/**
 * From server to client. One packet for each row in the result set.
 *
 * <pre>
 * Bytes                   Name
 * -----                   ----
 * n (Length Coded String) (column value)
 * ...
 *
 * (column value):         The data in the column, as a character string.
 *                         If a column is defined as non-character, the
 *                         server converts the value into a character
 *                         before sending it. Since the value is a Length
 *                         Coded String, a NULL can be represented with a
 *                         single byte containing 251(see the description
 *                         of Length Coded Strings in section "Elements" above).
 *
 * // * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#Row_Data_Packet
 * </pre>
 *
 * @author mycat
 */
public class RowDataPacket {
  private static final byte NULL_MARK = (byte) 251;
  private static final byte EMPTY_MARK = (byte) 0;

  public byte[] value;
  public int fieldCount;
  public final List<byte[]> fieldValues;

  public byte packetId = 0;

  public RowDataPacket(int fieldCount) {
    this.fieldCount = fieldCount;
    this.fieldValues = new ArrayList<byte[]>(fieldCount);
  }

  public void add(byte[] value) {
    //这里应该修改value
    fieldValues.add(value);
  }

  public void addFieldCount(int add) {
    //这里应该修改field
    fieldCount = fieldCount + add;
  }

  /**
   * TODO 待重构.
   */
  public int calcPacketSize() {
    int size = 0;
    for (int i = 0; i < fieldCount; i++) {
      byte[] v = fieldValues.get(i);
      size += (v == null || v.length == 0) ? 1 : BufferUtil.getLength(v);
    }
    return size;
  }

  /**
   * TODO 待重构.
   */
  public void write(ByteBuffer buffer) {
    int size = calcPacketSize();
    BufferUtil.writeUb3(buffer, size);
    buffer.put(packetId);
    for (int i = 0; i < fieldCount; i++) {
      byte[] fv = fieldValues.get(i);
      if (fv == null) {
        //buffer = bufferArray.checkWriteBuffer(1);
        buffer.put(RowDataPacket.NULL_MARK);
      } else if (fv.length == 0) {
        //buffer = bufferArray.checkWriteBuffer(1);
        buffer.put(RowDataPacket.EMPTY_MARK);
      } else {
        //buffer = bufferArray.checkWriteBuffer(BufferUtil
        //.getLength(fv.length));
        BufferUtil.writeLength(buffer, fv.length);
        buffer.put(fv);
      }
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
    for (int i = 0; i < fieldCount; i++) {
      byte[] fv = fieldValues.get(i);
      if (fv == null) {
        //buffer = bufferArray.checkWriteBuffer(1);
        buffer.put(RowDataPacket.NULL_MARK);
      } else if (fv.length == 0) {
        //buffer = bufferArray.checkWriteBuffer(1);
        buffer.put(RowDataPacket.EMPTY_MARK);
      } else {
        //buffer = bufferArray.checkWriteBuffer(BufferUtil
        //.getLength(fv.length));
        BufferUtil.writeLength(buffer, fv.length);
        buffer.put(fv);
      }
    }
    return buffer;
  }

}