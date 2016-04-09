package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class IntArray implements IntSource, DataView<int[]> {
  private final int[] array;

  public IntArray(int length) {
    this(new int[length]);
  }

  public IntArray(int[] array) {
    this.array = array;
  }

  @Override
  public int get(long index) {
    return array[Math.toIntExact(index)];
  }

  @Override
  public int[] getSource() {
    return array;
  }

  @Override
  public long getLength() {
    return array.length;
  }

  @Override
  public boolean isBigEndian() {
    return true;
  }

  @Override
  public boolean isGPUAccessible() {
    // Arrays are not guaranteed contiguous so a pointer isn't available to transfer to the GPU
    return false;
  }

  @Override
  public void set(long index, int value) {
    array[Math.toIntExact(index)] = value;
  }
}
