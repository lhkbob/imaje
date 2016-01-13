package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class ShortArray implements ShortSource {
  private final short[] array;

  public ShortArray(int length) {
    this(new short[length]);
  }

  public ShortArray(short[] array) {
    this.array = array;
  }

  @Override
  public short get(long index) {
    return array[Math.toIntExact(index)];
  }

  public short[] getArray() {
    return array;
  }

  @Override
  public long getLength() {
    return array.length;
  }

  @Override
  public void set(long index, short value) {
    array[Math.toIntExact(index)] = value;
  }
}
