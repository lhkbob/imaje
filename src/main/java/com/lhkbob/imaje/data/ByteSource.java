package com.lhkbob.imaje.data;

/**
 *
 */
public interface ByteSource extends BitDataSource, NumericDataSource {
  byte get(long index);

  void set(long index, byte value);

  @Override
  default int getBitSize() {
    return Byte.SIZE;
  }

  @Override
  default long getBits(long index) {
    return get(index);
  }

  @Override
  default void setBits(long index, long value) {
    set(index, (byte) value);
  }

  @Override
  default double getValue(long index) {
    return get(index);
  }

  @Override
  default void setValue(long index, double value) {
    // Clamp to byte boundary values so casting roll-over from double isn't surprising
    value = Math.max(Byte.MIN_VALUE, Math.min(value, Byte.MAX_VALUE));
    set(index, (byte) Math.round(value));
  }
}
