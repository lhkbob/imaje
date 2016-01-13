package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class LongArray implements LongSource {
  private final long[] array;

  public LongArray(int length) {
    this(new long[length]);
  }

  public LongArray(long[] array) {
    this.array = array;
  }

  @Override
  public long get(long index) {
    return array[Math.toIntExact(index)];
  }

  public long[] getArray() {
    return array;
  }

  @Override
  public long getLength() {
    return array.length;
  }

  @Override
  public void set(long index, long value) {
    array[Math.toIntExact(index)] = value;
  }
}
