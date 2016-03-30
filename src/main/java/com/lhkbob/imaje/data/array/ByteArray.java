package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.ByteSource;

/**
 *
 */
public class ByteArray implements ByteSource.Primitive {
  private final byte[] array;

  public ByteArray(int length) {
    this(new byte[length]);
  }

  public ByteArray(byte[] array) {
    this.array = array;
  }

  @Override
  public byte get(long index) {
    return array[Math.toIntExact(index)];
  }

  public byte[] getArray() {
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
  public void set(long index, byte value) {
    array[Math.toIntExact(index)] = value;
  }
}
