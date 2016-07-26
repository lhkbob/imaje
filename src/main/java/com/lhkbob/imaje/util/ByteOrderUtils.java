package com.lhkbob.imaje.util;

/**
 *
 */
public class ByteOrderUtils {
  public static double bytesToDouble(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8, boolean bigEndian) {
    if (bigEndian) {
      return bytesToDoubleBE(w1, w2, w3, w4, w5, w6, w7, w8);
    } else {
      return bytesToDoubleLE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  public static double bytesToDoubleBE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return Double.longBitsToDouble(bytesToLongBE(w1, w2, w3, w4, w5, w6, w7, w8));
  }

  public static double bytesToDoubleLE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return Double.longBitsToDouble(bytesToLongLE(w1, w2, w3, w4, w5, w6, w7, w8));
  }

  public static float bytesToFloat(byte w1, byte w2, byte w3, byte w4, boolean bigEndian) {
    if (bigEndian) {
      return bytesToFloatBE(w1, w2, w3, w4);
    } else {
      return bytesToFloatLE(w1, w2, w3, w4);
    }
  }

  public static float bytesToFloatBE(byte w1, byte w2, byte w3, byte w4) {
    return Float.intBitsToFloat(bytesToIntBE(w1, w2, w3, w4));
  }

  public static float bytesToFloatLE(byte w1, byte w2, byte w3, byte w4) {
    return Float.intBitsToFloat(bytesToIntLE(w1, w2, w3, w4));
  }

  public static int bytesToInt(byte w1, byte w2, byte w3, byte w4, boolean bigEndian) {
    if (bigEndian) {
      return bytesToIntBE(w1, w2, w3, w4);
    } else {
      return bytesToIntLE(w1, w2, w3, w4);
    }
  }

  public static int bytesToIntBE(byte w1, byte w2, byte w3, byte w4) {
    return (((0xff & w1) << 24) | ((0xff & w2) << 16) | ((0xff & w3) << 8) | (0xff & w4));
  }

  public static int bytesToIntLE(byte w1, byte w2, byte w3, byte w4) {
    return ((0xff & w1) | ((0xff & w2) << 8) | ((0xff & w3) << 16) | ((0xff & w4) << 24));
  }

  public static long bytesToLong(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8, boolean bigEndian) {
    if (bigEndian) {
      return bytesToLongBE(w1, w2, w3, w4, w5, w6, w7, w8);
    } else {
      return bytesToLongLE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  public static long bytesToLongBE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return (((0xffL & w1) << 56) | ((0xffL & w2) << 48) | ((0xffL & w3) << 40) | ((0xffL & w4)
        << 32) | ((0xffL & w5) << 24) | ((0xffL & w6) << 16) | ((0xffL & w7) << 8) | (0xffL & w8));
  }

  public static long bytesToLongLE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return ((0xffL & w1) | ((0xffL & w2) << 8) | ((0xffL & w3) << 16) | ((0xffL & w4) << 24) | (
        (0xffL & w5) << 32) | ((0xffL & w6) << 40) | ((0xffL & w7) << 48) | ((0xffL & w8) << 56));
  }

  public static short bytesToShort(byte w1, byte w2, boolean bigEndian) {
    if (bigEndian) {
      return bytesToShortBE(w1, w2);
    } else {
      return bytesToShortLE(w1, w2);
    }
  }

  public static short bytesToShortBE(byte w1, byte w2) {
    return (short) (((0xff & w1) << 8) | (0xff & w2));
  }

  public static short bytesToShortLE(byte w1, byte w2) {
    return (short) ((0xff & w1) | ((0xff & w2) << 8));
  }

  public static byte[] doubleToBytes(double value, boolean bigEndian) {
    if (bigEndian) {
      return doubleToBytesBE(value);
    } else {
      return doubleToBytesLE(value);
    }
  }

  public static byte[] doubleToBytesBE(double value) {
    return longToBytesBE(Double.doubleToLongBits(value));
  }

  public static byte[] doubleToBytesLE(double value) {
    return longToBytesLE(Double.doubleToLongBits(value));
  }

  public static byte[] floatToBytes(float value, boolean bigEndian) {
    if (bigEndian) {
      return floatToBytesBE(value);
    } else {
      return floatToBytesLE(value);
    }
  }

  public static byte[] floatToBytesBE(float value) {
    return intToBytesBE(Float.floatToIntBits(value));
  }

  public static byte[] floatToBytesLE(float value) {
    return intToBytesLE(Float.floatToIntBits(value));
  }

  public static byte[] intToBytes(int value, boolean bigEndian) {
    if (bigEndian) {
      return intToBytesBE(value);
    } else {
      return intToBytesLE(value);
    }
  }

  public static byte[] intToBytesBE(int value) {
    byte[] w = new byte[4];
    w[0] = (byte) (value >> 24);
    w[1] = (byte) (value >> 16);
    w[2] = (byte) (value >> 8);
    w[3] = (byte) value;
    return w;
  }

  public static byte[] intToBytesLE(int value) {
    byte[] w = new byte[4];
    w[0] = (byte) value;
    w[1] = (byte) (value >> 8);
    w[2] = (byte) (value >> 16);
    w[3] = (byte) (value >> 24);
    return w;
  }

  public static byte[] longToBytes(long value, boolean bigEndian) {
    if (bigEndian) {
      return longToBytesBE(value);
    } else {
      return longToBytesLE(value);
    }
  }

  public static byte[] longToBytesBE(long value) {
    byte[] w = new byte[8];
    w[0] = (byte) (value >> 56);
    w[1] = (byte) (value >> 48);
    w[2] = (byte) (value >> 40);
    w[3] = (byte) (value >> 32);
    w[4] = (byte) (value >> 24);
    w[5] = (byte) (value >> 16);
    w[6] = (byte) (value >> 8);
    w[7] = (byte) value;
    return w;
  }

  public static byte[] longToBytesLE(long value) {
    byte[] w = new byte[8];
    w[0] = (byte) value;
    w[1] = (byte) (value >> 8);
    w[2] = (byte) (value >> 16);
    w[3] = (byte) (value >> 24);
    w[4] = (byte) (value >> 32);
    w[5] = (byte) (value >> 40);
    w[6] = (byte) (value >> 48);
    w[7] = (byte) (value >> 56);
    return w;
  }

  public static byte[] shortToBytes(short value, boolean bigEndian) {
    if (bigEndian) {
      return shortToBytesBE(value);
    } else {
      return shortToBytesLE(value);
    }
  }

  public static byte[] shortToBytesBE(short value) {
    byte[] w = new byte[2];
    w[0] = (byte) (value >> 8);
    w[1] = (byte) value;
    return w;
  }

  public static byte[] shortToBytesLE(short value) {
    byte[] w = new byte[2];
    w[0] = (byte) value;
    w[1] = (byte) (value >> 8);
    return w;
  }
}
