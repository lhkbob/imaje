package com.lhkbob.imaje.data.types;

/**
 *
 */
public interface BinaryRepresentation {
  int getBitSize();

  double toNumericValue(long bits);

  long toBits(double value);

  double getMaxValue();

  double getMinValue();

  boolean isFloatingPoint();

  boolean isUnsigned();
}
