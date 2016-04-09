package com.lhkbob.imaje.data;

/**
 *
 */
public interface ShortSource extends BitDataSource, NumericDataSource {
  short get(long index);

  void set(long index, short value);

  @Override
  default int getBitSize() {
    return Short.SIZE;
  }

  @Override
  default long getBits(long index) {
    return get(index);
  }

  @Override
  default void setBits(long index, long value) {
    set(index, (short) value);
  }

  @Override
  default double getValue(long index) {
    return get(index);
  }

  @Override
  default void setValue(long index, double value) {
    // Clamp to short boundary values so casting roll-over from double isn't surprising
    value = Math.max(Short.MIN_VALUE, Math.min(value, Short.MAX_VALUE));
    set(index, (short) Math.round(value));
  }
}
