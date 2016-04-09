package com.lhkbob.imaje.data;

/**
 *
 */
public interface DoubleSource extends NumericDataSource {
  double get(long index);

  void set(long index, double value);

  @Override
  default int getBitSize() {
    return Double.SIZE;
  }

  @Override
  default double getValue(long index) {
    return get(index);
  }

  @Override
  default void setValue(long index, double value) {
    set(index, value);
  }
}
