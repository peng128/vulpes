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

package net.peng.vuples.jdbc.mysql.socket;

/**
 * Copy from Mycat.
 *
 * @author mycat
 */
public class RandomUtil {
  private static final byte[] bytes = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'q', 'w',
      'e', 'r', 't',
      'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c',
      'v', 'b', 'n', 'm',
      'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J',
      'K', 'L', 'Z', 'X',
      'C', 'V', 'B', 'N', 'M'};
  private static final long multiplier = 0x5DEECE66DL;
  private static final long addend = 0xBL;
  private static final long mask = (1L << 48) - 1;
  private static final long integerMask = (1L << 33) - 1;
  private static final long seedUniquifier = 8682522807148012L;

  private static long seed;

  static {
    long s = seedUniquifier + System.nanoTime();
    s = (s ^ multiplier) & mask;
    seed = s;
  }

  /**
   * Copy from Mycat.
   */
  public static final byte[] randomBytes(int size) {
    byte[] bb = bytes;
    byte[] ab = new byte[size];
    for (int i = 0; i < size; i++) {
      ab[i] = randomByte(bb);
    }
    return ab;
  }

  private static byte randomByte(byte[] b) {
    int ran = (int) ((next() & integerMask) >>> 16);
    return b[ran % b.length];
  }

  private static long next() {
    long oldSeed = seed;
    long nextSeed = 0L;
    do {
      nextSeed = (oldSeed * multiplier + addend) & mask;
    } while (oldSeed == nextSeed);
    seed = nextSeed;
    return nextSeed;
  }

  /**
   * 随机指定范围内N个不重复的数
   * 最简单最基本的方法.
   *
   * @param min 指定范围最小值（包含）
   * @param max 指定范围最大值(不包含)
   * @param n   随机数个数
   */
  public static int[] getNrandom(int min, int max, int n) {
    if (n > (max - min + 1) || max < min) {
      return null;
    }
    int[] result = new int[n];
    for (int i = 0; i < n; i++) {
      result[i] = -9999;
    }
    int count = 0;
    while (count < n) {
      int num = (int) ((Math.random() * (max - min)) + min);
      boolean flag = true;
      for (int j = 0; j < n; j++) {
        if (num == result[j]) {
          flag = false;
          break;
        }
      }
      if (flag) {
        result[count] = num;
        count++;
      }
    }
    return result;
  }
}