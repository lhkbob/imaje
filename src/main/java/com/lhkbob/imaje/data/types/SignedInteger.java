package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class SignedInteger implements BinaryRepresentation {
  private final int bits;
  private final long posMask;
  private final long negMask;
  private final double minValue;
  private final double maxValue;

  public SignedInteger(int bits) {
    Arguments.inRangeInclusive("bits", 1, 64, bits);

    this.bits = bits;
    maxValue = Math.pow(2.0, bits - 1) - 1.0;
    minValue = -Math.pow(2.0, bits - 1);
    // This forms a bit field of all 1s in the least signifcant positive 'bits' count bits.
    // It is done this way by using >>> on a full bitfield to work correctly when bits = 64
    posMask = Functions.maskLong(bits - 1);
    negMask = 1L << (bits - 1);
  }

  @Override
  public boolean isFloatingPoint() {
    return false;
  }

  @Override
  public boolean isUnsigned() {
    return false;
  }

  @Override
  public int getBitSize() {
    return bits;
  }

  @Override
  public double toNumericValue(long bits) {
    // First lift the positive portion of the bits to a double, and since posMask will have at most 63
    // bits in it, value will be correct
    double value = bits & posMask;
    if ((bits & negMask) != 0) {
      // Subtract off 2^(bits-1), which is equal to +minValue
      value += minValue;
    }
    return value;
  }

  @Override
  public long toBits(double value) {
    value = Functions.clamp(value, minValue, maxValue);
    // In both of the following cases, since value is between the minimum and maximum,
    // there is no need to apply the positive bit mask to the rounded long.
    if (value < 0.0) {
      // Round positive portion to a long and then OR in the sign bit
      value -= minValue;
      return negMask | Math.round(value);
    } else {
      // Just round the positive portion
      return Math.round(value);
    }
  }

  @Override
  public double getMaxValue() {
    return maxValue;
  }

  @Override
  public double getMinValue() {
    return minValue;
  }
}
