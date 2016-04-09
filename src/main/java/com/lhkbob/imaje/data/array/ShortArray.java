package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class ShortArray implements ShortSource, DataView<short[]> {
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

  @Override
  public short[] getSource() {
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
  public void set(long index, short value) {
    array[Math.toIntExact(index)] = value;
  }
}
