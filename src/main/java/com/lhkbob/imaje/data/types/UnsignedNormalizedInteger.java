package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class UnsignedNormalizedInteger implements BinaryRepresentation {
  private final UnsignedInteger unnormalized;

  public UnsignedNormalizedInteger(int bits) {
    unnormalized = new UnsignedInteger(bits);
  }

  @Override
  public int getBitSize() {
    return unnormalized.getBitSize();
  }

  @Override
  public double toNumericValue(long bits) {
    double unnorm = unnormalized.toNumericValue(bits);
    return unnorm / unnormalized.getMaxValue();
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
  public long toBits(double value) {
    value = Functions.clamp(value, 0.0, 1.0);
    return unnormalized.toBits(value * unnormalized.getMaxValue());
  }

  @Override
  public double getMaxValue() {
    return 1.0;
  }

  @Override
  public double getMinValue() {
    return 0.0;
  }
}
