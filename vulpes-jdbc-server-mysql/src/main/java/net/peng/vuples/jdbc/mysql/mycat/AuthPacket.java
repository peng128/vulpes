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
import lombok.ToString;
import net.peng.vuples.jdbc.mysql.socket.Capabilities;

/**
 * From client to server during initial handshake.
 * Bytes                        Name
 * -----                        ----
 * 4                            client_flags
 * 4                            max_packet_size
 * 1                            charset_number
 * 23                           (filler) always 0x00...
 * n (Null-Terminated String)   user
 * n (Length Coded Binary)      scramble_buff (1 + x bytes)
 * n (Null-Terminated String)   databasename (optional)
 *
 * @author mycat
 */
@ToString
public class AuthPacket {
  private static final byte[] FILLER = new byte[23];

  public long clientFlags;
  public long maxPacketSize;
  public int charsetIndex;
  public byte[] extra; // from FILLER(23)
  public String user;
  public byte[] password;
  public String database;
  public boolean allowMultiStatements;
  public String clientAuthPlugin;
  public int packetLength;
  public byte packetId;

  /**
   * Copy.
   */
  public void read(byte[] data) {
    MySqlMessage mm = new MySqlMessage(data);
    packetLength = mm.readUb3();
    packetId = mm.read();
    clientFlags = mm.readUb4();
    maxPacketSize = mm.readUb4();
    charsetIndex = (mm.read() & 0xff);
    // read extra
    int current = mm.position();
    int len = (int) mm.readLength();
    if (len > 0 && len < FILLER.length) {
      byte[] ab = new byte[len];
      System.arraycopy(mm.bytes(), mm.position(), ab, 0, len);
      this.extra = ab;
    }
    mm.position(current + FILLER.length);
    user = mm.readStringWithNull();
    password = mm.readBytesWithLength();
    if (((clientFlags & Capabilities.CLIENT_CONNECT_WITH_DB) != 0) && mm.hasRemaining()) {
      database = mm.readStringWithNull();
    }

    if ((clientFlags & Capabilities.CLIENT_MULTI_STATEMENTS) != 0) {
      allowMultiStatements = true;
    }

    if (((clientFlags & Capabilities.CLIENT_PLUGIN_AUTH) != 0) && mm.hasRemaining()) {
      clientAuthPlugin = mm.readStringWithNull();
    }
  }

  /**
   * Copy.
   */
  public ByteBuffer write() {
    ByteBuffer buffer = ByteBuffer.allocate(4096);
    BufferUtil.writeUb3(buffer, calcPacketSize());
    buffer.put(packetId);
    BufferUtil.writeUb4(buffer, clientFlags);
    BufferUtil.writeUb4(buffer, maxPacketSize);
    buffer.put((byte) charsetIndex);
    buffer = writeToBuffer(FILLER, buffer);
    if (user == null) {
      buffer.put((byte) 0);
    } else {
      byte[] userData = user.getBytes();
      BufferUtil.writeWithNull(buffer, userData);
    }
    if (password == null) {
      buffer.put((byte) 0);
    } else {
      BufferUtil.writeWithLength(buffer, password);
    }
    if (database == null) {
      buffer.put((byte) 0);
    } else {
      byte[] databaseData = database.getBytes();
      BufferUtil.writeWithNull(buffer, databaseData);
    }
    return buffer;
  }

  protected ByteBuffer writeToBuffer(byte[] src, ByteBuffer buffer) {
    int offset = 0;
    int length = src.length;
    int remaining = buffer.remaining();
    while (length > 0) {
      if (remaining >= length) {
        buffer.put(src, offset, length);
        break;
      }
    }
    return buffer;
  }

  protected int calcPacketSize() {
    int size = 32; // 4+4+1+23;
    size += (user == null) ? 1 : user.length() + 1;
    size += (password == null) ? 1 : BufferUtil.getLength(password);
    size += (database == null) ? 1 : database.length() + 1;
    return size;
  }

  protected String getPacketInfo() {
    return "MySQL Authentication Packet";
  }

}