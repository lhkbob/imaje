/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lhkbob.imaje.icc;

/**
 * TODO There are special integer encodings used for certain bit resolutions with PCSLab, etc. that
 * aren't 2's complement. Does this get handled on top of the ICCDataTypes or does it mean my general
 * treatment of integers etc is invalid?
 */
public class ICCDataTypeUtilTest {
  private static final double ERROR = 1e-16;
/*
  @Test
  public void testGetDateTimeNumber() {
    ZonedDateTime date = ICCDataTypeUtil.getDateTimeNumber(seq(
        shortToBytes(1996), shortToBytes(8), shortToBytes(23),
        shortToBytes(17), shortToBytes(45), shortToBytes(23)), 0);
    assertEquals(1996, date.getYear());
    assertEquals(Month.AUGUST, date.getMonth());
    assertEquals(23, date.getDayOfMonth());
    assertEquals(17, date.getHour());
    assertEquals(45, date.getMinute());
    assertEquals(23, date.getSecond());
  }

  @Test
  public void testGetFloat32Number() {
    assertEquals(0.0f, ICCDataTypeUtil.getFloat32Number(intToBytes(Float.floatToIntBits(0.0f)), 0), ERROR);
    assertEquals(1.0f, ICCDataTypeUtil.getFloat32Number(intToBytes(Float.floatToIntBits(1.0f)), 0), ERROR);
    assertEquals(1235.3529f, ICCDataTypeUtil.getFloat32Number(intToBytes(Float.floatToIntBits(
        1235.3529f)), 0), ERROR);
    assertEquals(
        -231235.863529f, ICCDataTypeUtil
            .getFloat32Number(intToBytes(Float.floatToIntBits(-231235.863529f)), 0),
        ERROR);
  }

  @Test
  public void testGetPositionNumber() {
    ICCDataTypeUtil.PositionNumber pn = ICCDataTypeUtil
        .getPositionNumber(seq(intToBytes(1234), intToBytes(5632)), 0);
    assertEquals(1234L, pn.getOffset());
    assertEquals(5632L, pn.getSize());
  }

  @Test
  public void testGetResponse16Number() {
    ICCDataTypeUtil.ResponseNumber rn = ICCDataTypeUtil
        .getResponse16Number(seq(shortToBytes(456), shortToBytes(0), intToBytes(0x10000)), 0);
    assertEquals(456, rn.getDeviceValue());
    assertEquals(1.0f, rn.getMeasurement(), ERROR);
  }

  @Test
  public void testGetS15Fixed16Number() {
    assertEquals(-32768.0f, ICCDataTypeUtil.getS15Fixed16Number(intToBytes(0x80000000), 0), ERROR);
    assertEquals(0.0f, ICCDataTypeUtil.getS15Fixed16Number(intToBytes(0), 0), ERROR);
    assertEquals(1.0f, ICCDataTypeUtil.getS15Fixed16Number(intToBytes(0x10000), 0), ERROR);
    assertEquals(32767.0f + 65535.0f / 65536.0f,
        ICCDataTypeUtil.getS15Fixed16Number(intToBytes(0x7fffffff), 0), ERROR);
  }

  @Test
  public void testGetU16Fixed16Number() {
    assertEquals(0.0f, ICCDataTypeUtil.getU16Fixed16Number(intToBytes(0), 0), ERROR);
    assertEquals(1.0f, ICCDataTypeUtil.getU16Fixed16Number(intToBytes(0x10000), 0), ERROR);
    assertEquals(65535.0f + 65535.0f / 65536.0f,
        ICCDataTypeUtil.getU16Fixed16Number(intToBytes(0xffffffff), 0), ERROR);
  }

  @Test
  public void testGetU1Fixed15Number() {
    assertEquals(0.0f, ICCDataTypeUtil.getU1Fixed15Number(shortToBytes(0), 0), ERROR);
    assertEquals(1.0f, ICCDataTypeUtil.getU1Fixed15Number(shortToBytes(0x8000), 0), ERROR);
    assertEquals(
        1.0f + 32767.0f / 32768.0f, ICCDataTypeUtil.getU1Fixed15Number(shortToBytes(0xffff), 0),
        ERROR);
  }

  @Test
  public void testGetU8Fixed8Number() {
    assertEquals(0.0f, ICCDataTypeUtil.getU8Fixed8Number(shortToBytes(0), 0), ERROR);
    assertEquals(1.0f, ICCDataTypeUtil.getU8Fixed8Number(shortToBytes(0x100), 0), ERROR);
    assertEquals(
        255.0f + 255.0f / 256.0f, ICCDataTypeUtil.getU8Fixed8Number(shortToBytes(0xffff), 0), ERROR);
  }

  @Test
  public void testGetUInt16Number() {
    assertEquals(0, ICCDataTypeUtil.getUInt16Number(shortToBytes(0), 0));
    assertEquals(255, ICCDataTypeUtil.getUInt16Number(shortToBytes(0xff), 0));
    assertEquals(32767, ICCDataTypeUtil.getUInt16Number(shortToBytes(0x7fff), 0));
    assertEquals(65535, ICCDataTypeUtil.getUInt16Number(shortToBytes(0xffff), 0));
  }

  @Test
  public void testGetUInt32Number() {
    assertEquals(0L, ICCDataTypeUtil.getUInt32Number(intToBytes(0), 0));
    assertEquals(255L, ICCDataTypeUtil.getUInt32Number(intToBytes(0xff), 0));
    assertEquals(32767L, ICCDataTypeUtil.getUInt32Number(intToBytes(0x7fff), 0));
    assertEquals(65535L, ICCDataTypeUtil.getUInt32Number(intToBytes(0xffff), 0));
    assertEquals((long) Integer.MAX_VALUE, ICCDataTypeUtil.getUInt32Number(intToBytes(0x7fffffff), 0));
    assertEquals(0xffffffffL, ICCDataTypeUtil.getUInt32Number(intToBytes(0xffffffff), 0));
  }

  @Test
  public void testGetUInt64Number() {
    assertEquals(0L, ICCDataTypeUtil.getUInt64Number(longToBytes(0), 0));
    assertEquals(255L, ICCDataTypeUtil.getUInt64Number(longToBytes(0xff), 0));
    assertEquals(32767L, ICCDataTypeUtil.getUInt64Number(longToBytes(0x7fff), 0));
    assertEquals(65535L, ICCDataTypeUtil.getUInt64Number(longToBytes(0xffff), 0));
    assertEquals((long) Integer.MAX_VALUE, ICCDataTypeUtil
        .getUInt64Number(longToBytes(0x7fffffff), 0));
    assertEquals(0xffffffffL, ICCDataTypeUtil.getUInt64Number(longToBytes(0xffffffffL), 0));
    assertEquals(0xff00ff00ffff00ffL, ICCDataTypeUtil
        .getUInt64Number(longToBytes(0xff00ff00ffff00ffL), 0));
  }

  @Test
  public void testGetUInt8Number() {
    assertEquals(0, ICCDataTypeUtil.getUInt8Number(byteToBytes(0), 0));
    assertEquals(127, ICCDataTypeUtil.getUInt8Number(byteToBytes(0x7f), 0));
    assertEquals(255, ICCDataTypeUtil.getUInt8Number(byteToBytes(0xff), 0));
  }

  @Test
  public void testGetXYZNumber() {
    ICCDataTypeUtil.XYZNumber xyz = ICCDataTypeUtil.getXYZNumber(seq(intToBytes(0x10000), intToBytes(0x10000),
        intToBytes(0x10000)), 0);
    assertEquals(1.0f, xyz.getX(), ERROR);
    assertEquals(1.0f, xyz.getY(), ERROR);
    assertEquals(1.0f, xyz.getZ(), ERROR);
  }

  private static byte[] byteToBytes(int bitsAsInt) {
    return new byte[] { (byte) bitsAsInt };
  }

  private static byte[] longToBytes(long bits) {
    byte[] data = new byte[8];
    data[0] = (byte) (bits >> 56);
    data[1] = (byte) (bits >> 48);
    data[2] = (byte) (bits >> 40);
    data[3] = (byte) (bits >> 32);
    data[4] = (byte) (bits >> 24);
    data[5] = (byte) (bits >> 16);
    data[6] = (byte) (bits >> 8);
    data[7] = (byte) (bits);
    return data;
  }

  private static byte[] intToBytes(int bits) {
    byte[] data = new byte[4];
    data[0] = (byte) (bits >> 24);
    data[1] = (byte) (bits >> 16);
    data[2] = (byte) (bits >> 8);
    data[3] = (byte) (bits);
    return data;
  }

  private static byte[] shortToBytes(int bitsAsInt) {
    short bits = (short) bitsAsInt;
    byte[] data = new byte[2];
    data[0] = (byte) (bits >> 8);
    data[1] = (byte) (bits);
    return data;
  }

  private static byte[] seq(byte[]... bytes) {
    int total = 0;
    for (byte[] b : bytes) {
      total += b.length;
    }

    byte[] packedBytes = new byte[total];
    int index = 0;
    for (byte[] b : bytes) {
      for (int i = 0; i < b.length; i++) {
        packedBytes[index++] = b[i];
      }
    }

    return packedBytes;
  }*/
}
