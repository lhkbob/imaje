package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 *
 */
public class ICCDataTypeUtil {
  public static class PositionNumber {
    private final long offset;
    private final long size;

    public PositionNumber(long offset, long size) {
      this.offset = offset;
      this.size = size;
    }

    public void configureBuffer(ByteBuffer data, int start) {
      data.limit(Math.toIntExact(start + offset + size));
      data.position(Math.toIntExact(start + offset));
    }

    public long getOffset() {
      return offset;
    }

    public long getSize() {
      return size;
    }
  }

  private ICCDataTypeUtil() {}

  // FIXME write unit tests for this one
  public static String nextASCIIString(ByteBuffer data, int maxStringLength) {
    if (maxStringLength >= 0) {
      require(data, maxStringLength);
    }

    StringBuilder sb = new StringBuilder();
    int i = 0;
    char c;
    while ((c = (char) data.get()) != '\0' && (maxStringLength < 0 || i < maxStringLength)) {
      sb.append(c);
      i++;
    }

    if (maxStringLength >= 0) {
      // If a maximum number of bytes is provided then this is a string within a fixed-length
      // byte field so advance to the end of the field even if the null character was found
      // before the limit was reached.
      skip(data, maxStringLength - sb.length());
    }

    return sb.toString();
  }

  public static ZonedDateTime nextDateTimeNumber(ByteBuffer data) {
    require(data, 12);
    int year = nextUInt16Number(data);
    int month = nextUInt16Number(data); // 1 - 12
    int dom = nextUInt16Number(data); // 1 - 31
    int hours = nextUInt16Number(data); // 0 - 23
    int minutes = nextUInt16Number(data);
    int seconds = nextUInt16Number(data);

    return ZonedDateTime.of(year, month, dom, hours, minutes, seconds, 0, ZoneOffset.UTC);
  }

  public static double nextFloat32Number(ByteBuffer data) {
    // This should not include NaNs, infinities, and un-normalized numbers but
    // otherwise follows IEEE 754 so we can just use this when interpreting the data.
    require(data, 4);
    return Float.intBitsToFloat((int) nextUInt32Number(data));
  }

  public static GenericColorValue nextLABNumber16(ByteBuffer data) {
    require(data, 6);
    double l = 100.0 * nextUInt16Number(data) / 65535.0;
    double a = nextUInt16Number(data) / 257.0 - 128.0;
    double b = nextUInt16Number(data) / 257.0 - 128.0;
    return GenericColorValue.pcsLAB(l, a, b);
  }

  public static GenericColorValue nextLABNumber8(ByteBuffer data) {
    require(data, 3);
    double l = 100.0 * nextUInt8Number(data) / 255.0;
    double a = nextUInt8Number(data) - 128.0;
    double b = nextUInt8Number(data) - 128.0;
    return GenericColorValue.pcsLAB(l, a, b);
  }

  public static GenericColorValue nextLABNumberFloat(ByteBuffer data) {
    return nextPCSNumberFloat(data, true);
  }

  public static GenericColorValue nextLABNumberLegacy16(ByteBuffer data) {
    require(data, 6);
    double l = 100.0f * data.get() / 255.0 + data.get() / 652.80;
    double a = nextU8Fixed8Number(data) - 128.0;
    double b = nextU8Fixed8Number(data) - 128.0;
    return GenericColorValue.pcsLAB(l, a, b);
  }

  public static PositionNumber nextPositionNumber(ByteBuffer data) {
    require(data, 8);
    long offset = nextUInt32Number(data);
    long length = nextUInt32Number(data);
    return new PositionNumber(offset, length);
  }

  public static double nextS15Fixed16Number(ByteBuffer data) {
    // Don't mask out the first byte since it contains the sign bit and we want
    // to preserve that.
    require(data, 4);
    int signedInt = (data.get() << 8) | (0xff & data.get());
    double fraction = nextUInt16Number(data) / 65536.0;
    return signedInt + fraction;
  }

  public static Signature nextSignature(ByteBuffer data) {
    return Signature.fromBitField(nextUInt32Number(data));
  }

  public static double nextU16Fixed16Number(ByteBuffer data) {
    require(data, 4);
    int unsignedInt = nextUInt16Number(data);
    double fraction = nextUInt16Number(data) / 65536.0;
    return unsignedInt + fraction;
  }

  public static double nextU1Fixed15Number(ByteBuffer data) {
    require(data, 2);
    int bitField = nextUInt16Number(data);
    int integer = ((bitField & 0x8000) == 0 ? 0 : 1);
    double fraction = (bitField & 0x7fff) / 32768.0;
    return integer + fraction;
  }

  public static double nextU8Fixed8Number(ByteBuffer data) {
    require(data, 2);
    int integer = (0xff & data.get());
    double fraction = (0xff & data.get()) / 256.0;
    return integer + fraction;
  }

  public static int nextUInt16Number(ByteBuffer data) {
    require(data, 2);
    return ((0xff & data.get()) << 8) | (0xff & data.get());
  }

  public static long nextUInt32Number(ByteBuffer data) {
    require(data, 4);
    return ((0xffL & data.get()) << 24) | ((0xffL & data.get()) << 16) | ((0xffL & data.get()) << 8)
        | (0xffL & data.get());
  }

  public static long nextUInt64Number(ByteBuffer data) {
    require(data, 8);
    return ((0xffL & data.get()) << 56) | ((0xffL & data.get()) << 48) | ((0xffL & data.get())
        << 40) | ((0xffL & data.get()) << 32) | ((0xffL & data.get()) << 24) | ((0xffL & data.get())
        << 16) | ((0xffL & data.get()) << 8) | (0xffL & data.get());
  }

  public static int nextUInt8Number(ByteBuffer data) {
    require(data, 1);
    return (0xff & data.get());
  }

  public static GenericColorValue nextXYZNumber(ByteBuffer data, GenericColorValue.ColorType type) {
    require(data, 12);
    double x = nextS15Fixed16Number(data);
    double y = nextS15Fixed16Number(data);
    double z = nextS15Fixed16Number(data);
    switch (type) {
    case CIEXYZ:
      return GenericColorValue.cieXYZ(x, y, z);
    case NORMALIZED_CIEXYZ:
      return GenericColorValue.nCIEXYZ(x, y, z);
    case PCSXYZ:
      return GenericColorValue.pcsXYZ(x, y, z);
    default:
      throw new IllegalArgumentException("Not an XYZ color type: " + type);
    }
  }

  public static GenericColorValue nextXYZNumber16(ByteBuffer data) {
    require(data, 6);
    double x = nextU1Fixed15Number(data);
    double y = nextU1Fixed15Number(data);
    double z = nextU1Fixed15Number(data);
    return GenericColorValue.pcsXYZ(x, y, z);
  }

  // FIXME write unit tests for new XYZ and LAB number functions
  public static GenericColorValue nextXYZNumberFloat(ByteBuffer data) {
    return nextPCSNumberFloat(data, false);
  }

  public static void require(ByteBuffer data, int requireAmount) {
    if (data.remaining() < requireAmount) {
      throw new IllegalArgumentException(
          "Requires " + requireAmount + " bytes to be available, but only has " + data.remaining());
    }
  }

  public static void skip(ByteBuffer data, int skipAmount) {
    data.position(data.position() + skipAmount);
  }

  public static void skipToBoundary(ByteBuffer data) {
    int remainder = data.position() % 4;
    // If remainder is 0 we don't want to skip ahead another 4 bytes
    // since we are already at a 4byte boundary
    if (remainder > 0) {
      skip(data, 4 - remainder);
    }
  }

  private static GenericColorValue nextPCSNumberFloat(ByteBuffer data, boolean lab) {
    require(data, 12);
    double c1 = nextFloat32Number(data);
    double c2 = nextFloat32Number(data);
    double c3 = nextFloat32Number(data);
    return (lab ? GenericColorValue.pcsLAB(c1, c2, c3) : GenericColorValue.pcsXYZ(c1, c2, c3));
  }
}
