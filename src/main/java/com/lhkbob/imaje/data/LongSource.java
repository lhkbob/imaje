package com.lhkbob.imaje.data;

/**
 *
 */
public interface LongSource extends BitDataSource, NumericDataSource {
  long get(long index);

  void set(long index, long value);

  @Override
  default int getBitSize() {
    return Long.SIZE;
  }

  @Override
  default long getBits(long index) {
    return get(index);
  }

  @Override
  default void setBits(long index, long value) {
    set(index, value);
  }

  @Override
  default double getValue(long index) {
    return get(index);
  }

  @Override
  default void setValue(long index, double value) {
    // Clamp to long boundary values so casting roll-over from double isn't surprising
    value = Math.max(Long.MIN_VALUE, Math.min(value, Long.MAX_VALUE));
    set(index, Math.round(value));
  }
}
