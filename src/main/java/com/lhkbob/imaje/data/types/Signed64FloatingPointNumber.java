package com.lhkbob.imaje.data.types;

/**
 *
 */
public class Signed64FloatingPointNumber implements BinaryRepresentation {
  @Override
  public int getBitSize() {
    return 64;
  }

  @Override
  public double toNumericValue(long bits) {
    return Double.longBitsToDouble(bits);
  }

  @Override
  public long toBits(double value) {
    return Double.doubleToRawLongBits(value);
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
  public boolean isFloatingPoint() {
    return true;
  }

  @Override
  public boolean isUnsigned() {
    return false;
  }
}
