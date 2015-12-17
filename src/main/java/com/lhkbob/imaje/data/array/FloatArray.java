package com.lhkbob.imaje.data.array;

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

  public float[] getArray() {
    return array;
  }

  @Override
  public long getLength() {
    return array.length;
  }

  @Override
  public float get(long index) {
    return array[Math.toIntExact(index)];
  }

  @Override
  public void set(long index, float value) {
    array[Math.toIntExact(index)] = value;
  }
}
