package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class SignedNormalizedInteger implements BinaryRepresentation {
  private final SignedInteger unnormalized;

  public SignedNormalizedInteger(int bits) {
    unnormalized = new SignedInteger(bits);
  }

  @Override
  public int getBitSize() {
    return unnormalized.getBitSize();
  }

  @Override
  public double toNumericValue(long bits) {
    // Clamp the scaled values since the distribution about 0 is uneven between positive
    // and negative axis.
    return Functions.clamp(unnormalized.toNumericValue(bits) / unnormalized.getMaxValue(), -1.0, 1.0);
  }

  @Override
  public long toBits(double value) {
    value = Functions.clamp(value, -1.0, 1.0);
    return unnormalized.toBits(value * unnormalized.getMaxValue());
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
  public double getMaxValue() {
    return 1.0;
  }

  @Override
  public double getMinValue() {
    return -1.0;
  }
}
