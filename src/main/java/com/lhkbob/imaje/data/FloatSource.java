package com.lhkbob.imaje.data;

/**
 *
 */
public interface FloatSource extends NumericDataSource {
  float get(long index);

  void set(long index, float value);

  @Override
  default int getBitSize() {
    return Float.SIZE;
  }

  default double getValue(long index) {
    return get(index);
  }

  default void setValue(long index, double value) {
    set(index, (float) value);
  }
}
