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
 * From Server To Client, part of Result Set Packets. One for each column in the
 * result set. Thus, if the value of field_columns in the Result Set Header
 * Packet is 3, then the Field Packet occurs 3 times.
 * Bytes                      Name
 * -----                      ----
 * n (Length Coded String)    catalog
 * n (Length Coded String)    db
 * n (Length Coded String)    table
 * n (Length Coded String)    org_table
 * n (Length Coded String)    name
 * n (Length Coded String)    org_name
 * 1                          (filler)
 * 2                          charsetNumber
 * 4                          length
 * 1                          type
 * 2                          flags
 * 1                          decimals
 * 2                          (filler), always 0x00
 * n (Length Coded Binary)    default
 *
 * @author mycat
 */
public class FieldPacket {
  public static final int UNSIGNED_FLAG = 0x0020;

  private static final byte[] DEFAULT_CATALOG = "def".getBytes();
  private static final byte[] FILLER = new byte[2];

  public byte[] catalog = DEFAULT_CATALOG;
  public byte[] db;
  public byte[] table;
  public byte[] orgTable;
  public byte[] name;
  public byte[] orgName;
  public int charsetIndex;
  public long length;
  public int type;
  public int flags;
  public byte decimals;
  public byte[] definition;
  public byte packetId;

  /**
   * Copy from mycat.
   */
  public int calcPacketSize() {
    int size = (catalog == null ? 1 : BufferUtil.getLength(catalog));
    size += (db == null ? 1 : BufferUtil.getLength(db));
    size += (table == null ? 1 : BufferUtil.getLength(table));
    size += (orgTable == null ? 1 : BufferUtil.getLength(orgTable));
    size += (name == null ? 1 : BufferUtil.getLength(name));
    size += (orgName == null ? 1 : BufferUtil.getLength(orgName));
    size += 13; // 1+2+4+1+2+1+2
    if (definition != null) {
      size += BufferUtil.getLength(definition);
    }
    return size;
  }

  private void writeBody(ByteBuffer buffer) {
    byte nullVal = 0;
    BufferUtil.writeWithLength(buffer, catalog, nullVal);
    BufferUtil.writeWithLength(buffer, db, nullVal);
    BufferUtil.writeWithLength(buffer, table, nullVal);
    BufferUtil.writeWithLength(buffer, orgTable, nullVal);
    BufferUtil.writeWithLength(buffer, name, nullVal);
    BufferUtil.writeWithLength(buffer, orgName, nullVal);
    buffer.put((byte) 0x0C);
    BufferUtil.writeUb2(buffer, charsetIndex);
    BufferUtil.writeUb4(buffer, length);
    buffer.put((byte) (type & 0xff));
    BufferUtil.writeUb2(buffer, flags);
    buffer.put(decimals);
    buffer.put((byte) 0x00);
    buffer.put((byte) 0x00);
    //buffer.position(buffer.position() + FILLER.length);
    if (definition != null) {
      BufferUtil.writeWithLength(buffer, definition);
    }
  }

  /**
   * Copy from Mycat.
   */
  public void write(ByteBuffer buffer) {
    int size = calcPacketSize();
    BufferUtil.writeUb3(buffer, size);
    buffer.put(packetId);
    writeBody(buffer);
  }

  /**
   * Copy from Mycat.
   */
  public ByteBuffer write() {
    int size = calcPacketSize();
    ByteBuffer buffer = ByteBuffer.allocate(size + 4);
    BufferUtil.writeUb3(buffer, size);
    buffer.put(packetId);
    writeBody(buffer);
    return buffer;
  }

}