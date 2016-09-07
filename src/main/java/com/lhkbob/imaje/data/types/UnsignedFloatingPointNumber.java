package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class UnsignedFloatingPointNumber implements BinaryRepresentation {
  private final SignedFloatingPointNumber base;
  private final long unsignedMask;

  public UnsignedFloatingPointNumber(int exponentBits, int mantissaBits) {
    base = new SignedFloatingPointNumber(exponentBits, mantissaBits);
    unsignedMask = Functions.maskLong(exponentBits + mantissaBits);
  }

  public UnsignedFloatingPointNumber(int exponentBits, int mantissaBits, boolean useLUT) {
    base = new SignedFloatingPointNumber(exponentBits, mantissaBits, useLUT);
    unsignedMask = Functions.maskLong(exponentBits + mantissaBits);
  }

  public UnsignedFloatingPointNumber(int exponentBits, int mantissaBits, boolean useExponentLUT, boolean useToDoubleLUT) {
    base = new SignedFloatingPointNumber(exponentBits, mantissaBits, useExponentLUT, useToDoubleLUT);
    unsignedMask = Functions.maskLong(exponentBits + mantissaBits);
  }

  @Override
  public boolean isFloatingPoint() {
    return true;
  }

  @Override
  public boolean isUnsigned() {
    return true;
  }

  @Override
  public int getBitSize() {
    // Remove the sign bit from the reported count
    return base.getBitSize() - 1;
  }

  @Override
  public double toNumericValue(long bits) {
    // Make sure to chop off any higher bits in case the signed float misinterprets it
    return base.toNumericValue(bits & unsignedMask);
  }

  @Override
  public long toBits(double value) {
    // First wrap the double value to be positive or zero; then any conversion to the signed
    // floating point format with the extra sign bit is equivalent to the unsigned format by just
    // ignoring the sign bit
    value = Functions.clamp(value, 0.0, Double.POSITIVE_INFINITY);
    return unsignedMask & base.toBits(value);
  }

  @Override
  public double getMaxValue() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double getMinValue() {
    return 0.0;
  }
}
