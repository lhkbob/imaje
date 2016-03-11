package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class FloatArray implements FloatSource {
  private final float[] array;

  public FloatArray(int length) {
    this(new float[length]);
  }

  public FloatArray(float[] array) {
    this.array = array;
  }

  @Override
  public float get(long index) {
    return array[Math.toIntExact(index)];
  }

  public float[] getArray() {
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
  public DataType getDataType() {
    return DataType.FLOAT32;
  }

  @Override
  public boolean isGPUAccessible() {
    // Arrays are not guaranteed contiguous so a pointer isn't available to transfer to the GPU
    return false;
  }

  @Override
  public void set(long index, float value) {
    array[Math.toIntExact(index)] = value;
  }
}
