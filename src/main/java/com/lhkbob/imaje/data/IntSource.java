package com.lhkbob.imaje.data;

/**
 *
 */
public interface IntSource extends BitDataSource, NumericDataSource {
  int get(long index);

  void set(long index, int value);

  @Override
  default int getBitSize() {
    return Integer.SIZE;
  }

  @Override
  default long getBits(long index) {
    return get(index);
  }

  @Override
  default void setBits(long index, long value) {
    set(index, (int) value);
  }

  @Override
  default double getValue(long index) {
    return get(index);
  }

  @Override
  default void setValue(long index, double value) {
    // Clamp to int boundary values so casting roll-over from double isn't surprising
    value = Math.max(Integer.MIN_VALUE, Math.min(value, Integer.MAX_VALUE));
    set(index, (int) Math.round(value));
  }
}
