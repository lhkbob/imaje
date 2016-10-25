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
package com.lhkbob.imaje.data;

import com.lhkbob.imaje.util.Arguments;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 */
public final class Bytes {
  private Bytes() {}

  public static double bytesToDoubleBE(byte[] data, int offset) {
    return bytesToDoubleBE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
        data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]);
  }

  public static double bytesToDoubleBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getDouble();
    } else {
      if (buffer.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      byte w5 = buffer.get();
      byte w6 = buffer.get();
      byte w7 = buffer.get();
      byte w8 = buffer.get();
      return bytesToDoubleBE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  public static double bytesToDoubleBE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return Double.longBitsToDouble(bytesToLongBE(w1, w2, w3, w4, w5, w6, w7, w8));
  }

  public static double bytesToDoubleLE(byte[] data, int offset) {
    return bytesToDoubleLE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
        data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]);
  }

  public static double bytesToDoubleLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getDouble();
    } else {
      if (buffer.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      byte w5 = buffer.get();
      byte w6 = buffer.get();
      byte w7 = buffer.get();
      byte w8 = buffer.get();
      return bytesToDoubleLE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  public static double bytesToDoubleLE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return Double.longBitsToDouble(bytesToLongLE(w1, w2, w3, w4, w5, w6, w7, w8));
  }

  public static float bytesToFloatBE(byte[] data, int offset) {
    return bytesToFloatBE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
  }

  public static float bytesToFloatBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getFloat();
    } else {
      if (buffer.remaining() < 4) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      return bytesToFloatBE(w1, w2, w3, w4);
    }
  }

  public static float bytesToFloatBE(byte w1, byte w2, byte w3, byte w4) {
    return Float.intBitsToFloat(bytesToIntBE(w1, w2, w3, w4));
  }

  public static float bytesToFloatLE(byte[] data, int offset) {
    return bytesToFloatLE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
  }

  public static float bytesToFloatLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getFloat();
    } else {
      if (buffer.remaining() < 4) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      return bytesToFloatLE(w1, w2, w3, w4);
    }
  }

  public static float bytesToFloatLE(byte w1, byte w2, byte w3, byte w4) {
    return Float.intBitsToFloat(bytesToIntLE(w1, w2, w3, w4));
  }

  public static int bytesToIntBE(byte[] data, int offset) {
    return bytesToIntBE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
  }

  public static int bytesToIntBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getInt();
    } else {
      if (buffer.remaining() < 4) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      return bytesToIntBE(w1, w2, w3, w4);
    }
  }

  public static int bytesToIntBE(byte w1, byte w2, byte w3, byte w4) {
    return (((0xff & w1) << 24) | ((0xff & w2) << 16) | ((0xff & w3) << 8) | (0xff & w4));
  }

  public static int bytesToIntLE(byte[] data, int offset) {
    return bytesToIntLE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
  }

  public static int bytesToIntLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getInt();
    } else {
      if (buffer.remaining() < 4) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      return bytesToIntLE(w1, w2, w3, w4);
    }
  }

  public static int bytesToIntLE(byte w1, byte w2, byte w3, byte w4) {
    return ((0xff & w1) | ((0xff & w2) << 8) | ((0xff & w3) << 16) | ((0xff & w4) << 24));
  }

  public static long bytesToLongBE(byte[] data, int offset) {
    return bytesToLongBE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
        data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]);
  }

  public static long bytesToLongBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getLong();
    } else {
      if (buffer.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      byte w5 = buffer.get();
      byte w6 = buffer.get();
      byte w7 = buffer.get();
      byte w8 = buffer.get();
      return bytesToLongBE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  public static long bytesToLongBE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return (((0xffL & w1) << 56) | ((0xffL & w2) << 48) | ((0xffL & w3) << 40) | ((0xffL & w4)
        << 32) | ((0xffL & w5) << 24) | ((0xffL & w6) << 16) | ((0xffL & w7) << 8) | (0xffL & w8));
  }

  public static long bytesToLongLE(byte[] data, int offset) {
    return bytesToLongLE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
        data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]);
  }

  public static long bytesToLongLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getLong();
    } else {
      if (buffer.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      byte w5 = buffer.get();
      byte w6 = buffer.get();
      byte w7 = buffer.get();
      byte w8 = buffer.get();
      return bytesToLongLE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  public static long bytesToLongLE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return ((0xffL & w1) | ((0xffL & w2) << 8) | ((0xffL & w3) << 16) | ((0xffL & w4) << 24) | (
        (0xffL & w5) << 32) | ((0xffL & w6) << 40) | ((0xffL & w7) << 48) | ((0xffL & w8) << 56));
  }

  public static short bytesToShortBE(byte[] data, int offset) {
    return bytesToShortBE(data[offset], data[offset + 1]);
  }

  public static short bytesToShortBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getShort();
    } else {
      if (buffer.remaining() < 2) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      return bytesToShortBE(w1, w2);
    }
  }

  public static short bytesToShortBE(byte w1, byte w2) {
    return (short) (((0xff & w1) << 8) | (0xff & w2));
  }

  public static short bytesToShortLE(byte[] data, int offset) {
    return bytesToShortLE(data[offset], data[offset + 1]);
  }

  public static short bytesToShortLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getShort();
    } else {
      if (buffer.remaining() < 2) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      return bytesToShortLE(w1, w2);
    }
  }

  public static short bytesToShortLE(byte w1, byte w2) {
    return (short) ((0xff & w1) | ((0xff & w2) << 8));
  }

  public static byte[] doubleToBytesBE(double value) {
    return longToBytesBE(Double.doubleToLongBits(value));
  }

  public static void doubleToBytesBE(double value, byte[] out, int offset) {
    longToBytesBE(Double.doubleToLongBits(value), out, offset);
  }

  public static void doubleToBytesBE(double value, ByteBuffer out) {
    longToBytesBE(Double.doubleToLongBits(value), out);
  }

  public static byte[] doubleToBytesLE(double value) {
    return longToBytesLE(Double.doubleToLongBits(value));
  }

  public static void doubleToBytesLE(double value, byte[] out, int offset) {
    longToBytesLE(Double.doubleToLongBits(value), out, offset);
  }

  public static void doubleToBytesLE(double value, ByteBuffer out) {
    longToBytesLE(Double.doubleToLongBits(value), out);
  }

  public static byte[] floatToBytesBE(float value) {
    return intToBytesBE(Float.floatToIntBits(value));
  }

  public static void floatToBytesBE(float value, byte[] out, int offset) {
    intToBytesBE(Float.floatToIntBits(value), out, offset);
  }

  public static void floatToBytesBE(float value, ByteBuffer out) {
    intToBytesBE(Float.floatToIntBits(value), out);
  }

  public static byte[] floatToBytesLE(float value) {
    return intToBytesLE(Float.floatToIntBits(value));
  }

  public static void floatToBytesLE(float value, byte[] out, int offset) {
    intToBytesLE(Float.floatToIntBits(value), out, offset);
  }

  public static void floatToBytesLE(float value, ByteBuffer out) {
    intToBytesLE(Float.floatToIntBits(value), out);
  }

  public static void intToBytesBE(int value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 4);
    out[offset] = (byte) (value >> 24);
    out[offset + 1] = (byte) (value >> 16);
    out[offset + 2] = (byte) (value >> 8);
    out[offset + 3] = (byte) value;
  }

  public static byte[] intToBytesBE(int value) {
    byte[] out = new byte[4];
    intToBytesBE(value, out, 0);
    return out;
  }

  public static void intToBytesBE(int value, ByteBuffer out) {
    if (out.order() == ByteOrder.BIG_ENDIAN) {
      out.putInt(value);
    } else {
      if (out.remaining() < 4) {
        throw new BufferUnderflowException();
      }
      out.put((byte) (value >> 24));
      out.put((byte) (value >> 16));
      out.put((byte) (value >> 8));
      out.put((byte) value);
    }
  }

  public static void intToBytesLE(int value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 4);
    out[offset] = (byte) value;
    out[offset + 1] = (byte) (value >> 8);
    out[offset + 2] = (byte) (value >> 16);
    out[offset + 3] = (byte) (value >> 24);
  }

  public static byte[] intToBytesLE(int value) {
    byte[] out = new byte[4];
    intToBytesLE(value, out, 0);
    return out;
  }

  public static void intToBytesLE(int value, ByteBuffer out) {
    if (out.order() == ByteOrder.LITTLE_ENDIAN) {
      out.putInt(value);
    } else {
      if (out.remaining() < 4) {
        throw new BufferUnderflowException();
      }
      out.put((byte) value);
      out.put((byte) (value >> 8));
      out.put((byte) (value >> 16));
      out.put((byte) (value >> 24));
    }
  }

  public static void longToBytesBE(long value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 8);
    out[offset] = (byte) (value >> 56);
    out[offset + 1] = (byte) (value >> 48);
    out[offset + 2] = (byte) (value >> 40);
    out[offset + 3] = (byte) (value >> 32);
    out[offset + 4] = (byte) (value >> 24);
    out[offset + 5] = (byte) (value >> 16);
    out[offset + 6] = (byte) (value >> 8);
    out[offset + 7] = (byte) value;
  }

  public static byte[] longToBytesBE(long value) {
    byte[] out = new byte[8];
    longToBytesBE(value, out, 0);
    return out;
  }

  public static void longToBytesBE(long value, ByteBuffer out) {
    if (out.order() == ByteOrder.BIG_ENDIAN) {
      out.putLong(value);
    } else {
      if (out.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      out.put((byte) (value >> 56));
      out.put((byte) (value >> 48));
      out.put((byte) (value >> 32));
      out.put((byte) (value >> 24));
      out.put((byte) (value >> 16));
      out.put((byte) (value >> 8));
      out.put((byte) value);
    }
  }

  public static void longToBytesLE(long value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 8);
    out[offset] = (byte) value;
    out[offset + 1] = (byte) (value >> 8);
    out[offset + 2] = (byte) (value >> 16);
    out[offset + 3] = (byte) (value >> 24);
    out[offset + 4] = (byte) (value >> 32);
    out[offset + 5] = (byte) (value >> 40);
    out[offset + 6] = (byte) (value >> 48);
    out[offset + 7] = (byte) (value >> 56);
  }

  public static byte[] longToBytesLE(long value) {
    byte[] out = new byte[8];
    longToBytesLE(value, out, 0);
    return out;
  }

  public static void longToBytesLE(long value, ByteBuffer out) {
    if (out.order() == ByteOrder.LITTLE_ENDIAN) {
      out.putLong(value);
    } else {
      if (out.remaining() < 8) {
        throw new BufferUnderflowException();
      }
      out.put((byte) value);
      out.put((byte) (value >> 8));
      out.put((byte) (value >> 16));
      out.put((byte) (value >> 24));
      out.put((byte) (value >> 32));
      out.put((byte) (value >> 40));
      out.put((byte) (value >> 48));
      out.put((byte) (value >> 56));
    }
  }

  public static void shortToBytesBE(short value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 2);
    out[offset] = (byte) (value >> 8);
    out[offset + 1] = (byte) value;
  }

  public static byte[] shortToBytesBE(short value) {
    byte[] out = new byte[2];
    shortToBytesBE(value, out, 0);
    return out;
  }

  public static void shortToBytesBE(short value, ByteBuffer out) {
    if (out.order() == ByteOrder.BIG_ENDIAN) {
      out.putShort(value);
    } else {
      if (out.remaining() < 2) {
        throw new BufferUnderflowException();
      }
      out.put((byte) (value >> 8));
      out.put((byte) value);
    }
  }

  public static void shortToBytesLE(short value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 2);
    out[offset] = (byte) value;
    out[offset + 1] = (byte) (value >> 8);
  }

  public static byte[] shortToBytesLE(short value) {
    byte[] out = new byte[2];
    shortToBytesLE(value, out, 0);
    return out;
  }

  public static void shortToBytesLE(short value, ByteBuffer out) {
    if (out.order() == ByteOrder.LITTLE_ENDIAN) {
      out.putShort(value);
    } else {
      if (out.remaining() < 2) {
        throw new BufferUnderflowException();
      }
      out.put((byte) value);
      out.put((byte) (value >> 8));
    }
  }
}
