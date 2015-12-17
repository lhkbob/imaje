package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class IntArray implements IntSource {
  private final int[] array;

  public IntArray(int length) {
    this(new int[length]);
  }

  public IntArray(int[] array) {
    this.array = array;
  }

  public int[] getArray() {
    return array;
  }

  @Override
  public long getLength() {
    return array.length;
  }

  @Override
  public int get(long index) {
    return array[Math.toIntExact(index)];
  }

  @Override
  public void set(long index, int value) {
    array[Math.toIntExact(index)] = value;
  }
}
