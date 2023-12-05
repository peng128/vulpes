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
 * Copy from mycat.
 *
 * @author mycat
 */
public class BufferUtil {

  /**
   * Copy from mycat.
   *
   */
  public static final void writeUb2(ByteBuffer buffer, int i) {
    buffer.put((byte) (i & 0xff));
    buffer.put((byte) (i >>> 8));
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeUb3(ByteBuffer buffer, int i) {
    buffer.put((byte) (i & 0xff));
    buffer.put((byte) (i >>> 8));
    buffer.put((byte) (i >>> 16));
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeInt(ByteBuffer buffer, int i) {
    buffer.put((byte) (i & 0xff));
    buffer.put((byte) (i >>> 8));
    buffer.put((byte) (i >>> 16));
    buffer.put((byte) (i >>> 24));
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeFloat(ByteBuffer buffer, float f) {
    writeInt(buffer, Float.floatToIntBits(f));
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeUb4(ByteBuffer buffer, long l) {
    buffer.put((byte) (l & 0xff));
    buffer.put((byte) (l >>> 8));
    buffer.put((byte) (l >>> 16));
    buffer.put((byte) (l >>> 24));
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeLong(ByteBuffer buffer, long l) {
    buffer.put((byte) (l & 0xff));
    buffer.put((byte) (l >>> 8));
    buffer.put((byte) (l >>> 16));
    buffer.put((byte) (l >>> 24));
    buffer.put((byte) (l >>> 32));
    buffer.put((byte) (l >>> 40));
    buffer.put((byte) (l >>> 48));
    buffer.put((byte) (l >>> 56));
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeDouble(ByteBuffer buffer, double d) {
    writeLong(buffer, Double.doubleToLongBits(d));
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeLength(ByteBuffer buffer, long l) {
    if (l < 0) {
      buffer.put((byte) 254);
      writeLong(buffer, l);
    } else if (l < 251) {
      buffer.put((byte) l);
    } else if (l < 0x10000L) {
      buffer.put((byte) 252);
      writeUb2(buffer, (int) l);
    } else if (l < 0x1000000L) {
      buffer.put((byte) 253);
      writeUb3(buffer, (int) l);
    } else {
      buffer.put((byte) 254);
      writeLong(buffer, l);
    }
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeWithNull(ByteBuffer buffer, byte[] src) {
    for (byte b : src) {
      buffer.put(b);
    }
    buffer.put((byte) 0);
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeWithLength(ByteBuffer buffer, byte[] src) {
    int length = src.length;
    if (length < 251) {
      buffer.put((byte) length);
    } else if (length < 0x10000L) {
      buffer.put((byte) 252);
      writeUb2(buffer, length);
    } else if (length < 0x1000000L) {
      buffer.put((byte) 253);
      writeUb3(buffer, length);
    } else {
      buffer.put((byte) 254);
      writeLong(buffer, length);
    }
    buffer.put(src);
  }

  /**
   * Copy from mycat.
   *
   */
  public static final void writeWithLength(ByteBuffer buffer, byte[] src, byte nullValue) {
    if (src == null) {
      buffer.put(nullValue);
    } else {
      writeWithLength(buffer, src);
    }
  }

  /**
   * Copy from mycat.
   *
   */
  public static final int getLength(long length) {
    if (length < 0) {
      return 9;
    } else if (length < 251) {
      return 1;
    } else if (length < 0x10000L) {
      return 3;
    } else if (length < 0x1000000L) {
      return 4;
    } else {
      return 9;
    }
  }

  /**
   * Copy from mycat.
   *
   */
  public static final int getLength(byte[] src) {
    int length = src.length;
    if (length < 251) {
      return 1 + length;
    } else if (length < 0x10000L) {
      return 3 + length;
    } else if (length < 0x1000000L) {
      return 4 + length;
    } else {
      return 9 + length;
    }
  }

}