package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class UnsignedInteger implements BinaryRepresentation {
  private static final long SIGN_MASK_64 = ~(1L << 63);
  private static final double SIGN_OFFSET_64 = Math.pow(2.0, 63.0);
  // The highest double value that can be represented as a regular signed long, e.g. Long.MAX_VALUE
  // which is equal to 2^63 - 1, but since we round the double value anything higher than
  // 2^63-0.5 will round to 2^63 and then overflow, requiring special handling.
  private static final double SIGN_THRESHOLD_64 = SIGN_OFFSET_64 - 0.5;

  private final int bits;
  private final long mask;
  private final double maxValue;

  public UnsignedInteger(int bits) {
    if (bits > 64)
      throw new IllegalArgumentException("Cannot convert integers requiring more than 64 bits: " + bits);
    if (bits < 1)
      throw new IllegalArgumentException("Bit count must be at least 1: " + bits);

    this.bits = bits;
    maxValue = Math.pow(2.0, bits) - 1.0;
    // This forms a bit field of all 1s in the least signifcant 'bits' count bits.
    // It is done this way by using >>> on a full bitfield to work correctly when bits = 64
    mask = Functions.maskLong(bits);
  }

  @Override
  public int getBitSize() {
    return bits;
  }

  @Override
  public double toNumericValue(long bits) {
    if (this.bits == 64 && bits < 0) {
      // Special handling to convert Java's 2's complement bit into a positive value,
      // by masking off the upper most bit and add 2^63 to it
      return SIGN_OFFSET_64 + (bits & SIGN_MASK_64);
    } else {
      // Remove any excess bits and then auto lift to a double, which will be the proper value
      // since the masked bits must be positive at this point
      return (mask & bits);
    }
  }

  @Override
  public long toBits(double value) {
    value = Functions.clamp(value, 0.0, maxValue);

    // This check will only ever be true if bits == 64
    if (value > SIGN_THRESHOLD_64) {
      // Round the lower 63 bits to a long and then OR in the highest "sign" bit.
      long lower = Math.round(value - SIGN_THRESHOLD_64);
      return (1L << 63) | lower;
    } else {
      // Since the double value has already been clamped to a valid range, there's no need
      // to AND the mask to the rounded value.
      return Math.round(value);
    }
  }

  @Override
  public boolean isFloatingPoint() {
    return false;
  }

  @Override
  public boolean isUnsigned() {
    return true;
  }

  @Override
  public double getMaxValue() {
    return maxValue;
  }

  @Override
  public double getMinValue() {
    return 0.0;
  }
}
