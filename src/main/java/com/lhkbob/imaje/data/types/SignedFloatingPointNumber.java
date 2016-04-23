package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.util.Functions;

import static java.lang.Double.doubleToRawLongBits;

/**
 *
 */
public class SignedFloatingPointNumber implements BinaryRepresentation {
  // Will be null if getBitSize() > 16
  private final double[] customToDoubleLUT;
  // The exponent value for denormalized floating point values.
  private final int denormedExponent;
  // The difference between a 64-bit double exponent bias and the custom floating point exponent
  // bias.  Adding it to custom exponent converts it to a 64-bit biased exponent; subtracting it
  // converts a 64-bit exponent to a biased custom exponent.
  private final long exponentBiasConversion;
  // Number of bits in the exponent field, which is just to the right of the most significant sign bit.
  private final int exponentBits;
  // A mask of exponentBits 1s in the LSBs.
  private final long exponentMask;
  // The exponent mask shifted to the left by the number of mantissa bits so it is the actual
  // exponent field. This also represents the exponent value+loc for non-zero special float values.
  private final long exponentMaskShifted;
  // The bit location of the implicit '1.' in a normalized float. This value is a single 1 bit
  // just to the left of the mantissa field.
  private final long implicitMantissaBit;
  private final long lutMask; // A mask over the full bitfield of the float layout
  // Number of bits in the mantissa, which is the least significant bits.
  private final int mantissaBits;
  // The difference between a 64-bit double mantissa and the custom float mantissa lengths. This
  // is the amount to shift left a custom mantissa to make a double mantissa. Or to shift right
  // to truncate to a custom mantissa from a double.
  private final int mantissaConversionShift;
  // A mask of mantissaBits 1s in the LSBs (does not need to be shifted because the mantissa is
  // stored in the LSBs).
  private final long mantissaMask;
  // How much to shift the bit field to the right to extract the sign with SIGN_MASK, or to shift
  // to the left to place in the appropriate field of the custom float type.
  private final int signShift;
  // This LUT is keyed by the combined sign and exponent bits of a 64-bit double, which is only
  // 4096 values and thus reasonable. A 0 denotes the fast-path relying on the returned sign+exponent
  // is invalid and the general function must be used.
  private final long[] signedExponentLUT;

  public SignedFloatingPointNumber(int exponentBits, int mantissaBits) {
    this(exponentBits, mantissaBits, (1 + exponentBits + mantissaBits) <= 16);
  }

  public SignedFloatingPointNumber(int exponentBits, int mantissaBits, boolean useLUT) {
    this(exponentBits, mantissaBits, useLUT, useLUT);
  }

  public SignedFloatingPointNumber(
      int exponentBits, int mantissaBits, boolean useExponentLUT, boolean useToDoubleLUT) {
    // These restrictions are in place because the conversion logic assumes that a Java double
    // has more precision in both the exponent and mantissa of the custom float type.
    if (exponentBits >= DOUBLE_EXPONENT_BITS) {
      throw new UnsupportedOperationException(
          "exponentBits must be less than exponent size of a Java double (" + DOUBLE_EXPONENT_BITS
              + "): " + exponentBits);
    }
    if (mantissaBits >= DOUBLE_MANTISSA_BITS) {
      throw new UnsupportedOperationException(
          "mantissaBits must be less than mantissa size of a Java double (" + DOUBLE_MANTISSA_BITS
              + "): " + mantissaBits);
    }

    this.exponentBits = exponentBits;
    this.mantissaBits = mantissaBits;

    mantissaMask = Functions.maskLong(mantissaBits);
    exponentMask = Functions.maskLong(exponentBits);
    exponentMaskShifted = exponentMask << mantissaBits;

    exponentBiasConversion = DOUBLE_EXPONENT_BIAS - ((1L << (exponentBits - 1)) - 1);
    mantissaConversionShift = DOUBLE_MANTISSA_BITS - mantissaBits;
    signShift = mantissaBits + exponentBits;
    denormedExponent = (1 << (exponentBits - 1)) - 2;
    implicitMantissaBit = 1L << mantissaBits;

    if (useToDoubleLUT && getBitSize() <= 16) {
      // It is reasonable to precalculate all possible values of the custom float type and store
      // them in a lookup table.
      customToDoubleLUT = new double[1 << getBitSize()];
      for (int i = 0; i < customToDoubleLUT.length; i++) {
        customToDoubleLUT[i] = Double.longBitsToDouble(toJavaDoubleBits(i));
      }
      lutMask = Functions.maskLong(1 + exponentBits + mantissaBits);
    } else {
      customToDoubleLUT = null;
      lutMask = 0;
    }

    if (useExponentLUT) {
      signedExponentLUT = new long[1 << (DOUBLE_EXPONENT_BITS + 1)];
      int halfTableLength = 1 << DOUBLE_EXPONENT_BITS;
      for (int i = 0; i < halfTableLength; i++) {
        long e = (i & DOUBLE_EXPONENT_MASK) - exponentBiasConversion;

        if (e <= 0 || e >= exponentMask) {
          // A special case that can't just exist as a normalized custom float
          signedExponentLUT[i] = 0; // Positive sign
          signedExponentLUT[i | halfTableLength] = 0; // Negative sign
        } else {
          // Within normal range of custom float exponent, so shift the sign and
          // exponent into the required position for the custom layout.
          signedExponentLUT[i] = e << mantissaBits; // Positive case
          signedExponentLUT[i | halfTableLength] =
              (e << mantissaBits) | (1L << signShift); // Negative case
        }
      }
    } else {
      signedExponentLUT = null;
    }
  }

  @Override
  public boolean isFloatingPoint() {
    return true;
  }

  @Override
  public boolean isUnsigned() {
    return false;
  }

  @Override
  public int getBitSize() {
    return exponentBits + mantissaBits + 1;
  }

  @Override
  public double getMaxValue() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double getMinValue() {
    return Double.NEGATIVE_INFINITY;
  }

  @Override
  public long toBits(double value) {
    long doubleBits = doubleToRawLongBits(value);
    if (value == 0.0) {
      // Special case for 0, but preserve the sign bit; when this is true all bits are 0
      // except possibly the MSB
      return doubleBits >>> (DOUBLE_SIGN_SHIFT - signShift);
    } else {
      if (signedExponentLUT != null) {
        // Try and see if the signed-exponent can be represented as a normalized float
        long se = doubleBits >>> DOUBLE_MANTISSA_BITS;
        se = signedExponentLUT[(int) se];
        if (se != 0) {
          // Normal case, so round the mantissa and combine with previously computed exponent
          return se | roundToNearestEven(
              doubleBits & DOUBLE_MANTISSA_MASK, mantissaConversionShift);
        }
      }
      // No fast path
      return fromJavaDoubleBits(doubleBits);
    }
  }

  @Override
  public double toNumericValue(long bits) {
    if (customToDoubleLUT != null) {
      // Just do a lookup
      int index = (int) (bits & lutMask);
      return customToDoubleLUT[index];
    } else {
      // General function
      return Double.longBitsToDouble(toJavaDoubleBits(bits));
    }
  }

  private long fromJavaDoubleBits(long bits) {
    // s, e, and m represent the sign, exponent, and mantissa of a Java double
    // and must be transformed into the range and bit location for the custom float number.
    long s = (bits >> DOUBLE_SIGN_SHIFT) & SIGN_MASK;
    long e = (bits >> DOUBLE_MANTISSA_BITS) & DOUBLE_EXPONENT_MASK;
    long m = (bits & DOUBLE_MANTISSA_MASK);

    boolean isSpecial = e == DOUBLE_EXPONENT_MASK;

    // Shift s into the sign location for the custom type
    s = s << signShift;
    // Remove double's exponent bias and apply bias for the custom type
    e = e - exponentBiasConversion;

    if (e <= 0) {
      if (e < -mantissaBits) {
        // Exponent is less than precision of custom float, even as a denormalized number
        // so return it as an appropriately signed zero.
        //
        // This case covers all denormalized double values since the normal exponent range of a double
        // will always be sufficient to represent the custom float
        return s;
      }

      // Add a bit to the mantissa to make the implicit 1. explicit
      m = m | DOUBLE_IMPLICIT_BIT;
      // How many places to shift the mantissa given the fixed exponent
      int denormShift = denormedExponent - (int) e;
      // Since denormShift > mantissaConversionShift any overflow from rounding
      // will not push past the boundary of the mantissa in this case
      return s | roundToNearestEven(m, denormShift);
    } else if (isSpecial) {
      // Double is special, either NaN or +/- infinity
      if (m == 0) {
        // Return a signed infinity
        return s | exponentMaskShifted;
      } else {
        // Make it a custom NaN that preserves the most significant possible mantissa bits
        m = m >> mantissaConversionShift;
        if (m == 0) {
          // Make sure the mantissa is non-zero or it will appear to be infinity
          m = 1;
        }
        return s | exponentMaskShifted | m;
      }
    } else {
      // Exponent is positive and a normal double should be convertable to a normalized custom float.
      // First round mantissa to the requisite number of bits and apply overflow to the exponent.
      // If the exponent also overflows then a signed infinity.

      m = roundToNearestEven(m, mantissaConversionShift);
      if (m > mantissaMask) {
        // Mantissa overflow
        m = 0;
        e += 1;
      }

      if (e >= exponentMask) {
        // Overflow in the exponent so signed infinity
        return s | exponentMaskShifted;
      } else {
        return s | (e << mantissaBits) | m;
      }
    }
  }

  private long toJavaDoubleBits(long bits) {
    // s, e, and m represent the sign, exponent, and mantissa of the custom float number
    // and must be transformed into the range valid for a Java double.
    long s = (bits >> signShift) & SIGN_MASK;
    long e = (bits >> mantissaBits) & exponentMask;
    long m = (bits & mantissaMask);
    boolean isSpecial = e == exponentMask;

    if (e == 0) {
      if (m == 0) {
        // +/- 0, so the Java double has a 0 exponent and mantissa and
        // preserves the sign in bit 63.
        return s << DOUBLE_SIGN_SHIFT;
      } else {
        // A denormalized number, so renormalize it by multiplying the mantissa by 2
        // and dividing the exponent by 2 (no net change but moves the mantissa's
        // decimal to the right) until a 1 bit has been shifted passed the decimal point.
        while ((m & implicitMantissaBit) == 0) {
          // Shift mantissa to the left and decrease the exponent by 1
          m <<= 1;
          e--;
        }

        // Once the loop has exited, there is a 1 set in the bit to the left of the mantissa
        // range (the implicit normalization bit is currently explicit). To make it
        // implicit, set it to 0 and increase the exponent by 1.
        m &= ~implicitMantissaBit;
        e++;
      }
    } else if (isSpecial) {
      if (m == 0) {
        // Positive or negative infinity, so return a double that has a 0 mantissa and
        // filled in exponent, while preserving the sign bit
        return (s << DOUBLE_SIGN_SHIFT) | DOUBLE_EXPONENT_MASK_SHIFTED;
      } else {
        // NaN - preserve sign and any set bits in m
        return (s << DOUBLE_SIGN_SHIFT) | DOUBLE_EXPONENT_MASK_SHIFTED | (m
            << mantissaConversionShift);
      }
    }

    // A normalized float, so transform the exponent into the double's offset
    // and shift the mantissa significant bits to the start of the double's mantissa
    e = e + exponentBiasConversion;
    return (s << DOUBLE_SIGN_SHIFT) | (e << DOUBLE_MANTISSA_BITS) | (m << mantissaConversionShift);
  }

  private static long roundToNearestEven(long bits, int toDiscard) {
    // Form a value that will overflow into the rounding bit if any of the
    // even lower significant bits are 1s.
    long a = Functions.maskLong(toDiscard - 1);
    // Grab the LSB of the kept bits; if it is 1 then adding it back to the
    // final sum will either have no effect, or properly trigger further rounding
    // by rounding up to a larger even value.
    long b = (bits >> toDiscard) & 1;
    // A round-up to even may have overflowed past the valid MSB of the mantissa so code must
    // check for that condition.
    return (bits + a + b) >> toDiscard;
  }

  // These values parallel the members except they are specific to Java's 64-bit double
  private static final int DOUBLE_EXPONENT_BITS = 11;
  private static final long DOUBLE_EXPONENT_BIAS = (1L << (DOUBLE_EXPONENT_BITS - 1)) - 1;
  private static final long DOUBLE_EXPONENT_MASK = Functions.maskLong(DOUBLE_EXPONENT_BITS);
  private static final int DOUBLE_MANTISSA_BITS = 52;
  private static final long DOUBLE_EXPONENT_MASK_SHIFTED =
      DOUBLE_EXPONENT_MASK << DOUBLE_MANTISSA_BITS;
  private static final long DOUBLE_IMPLICIT_BIT = 1L << DOUBLE_MANTISSA_BITS;
  private static final long DOUBLE_MANTISSA_MASK = Functions.maskLong(DOUBLE_MANTISSA_BITS);
  private static final int DOUBLE_SIGN_SHIFT = 63;
  private static final long SIGN_MASK = 1L;
}
