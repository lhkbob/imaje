package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class DoubleArray implements DoubleSource {
  private final double[] array;

  public DoubleArray(int length) {
    this(new double[length]);
  }

  public DoubleArray(double[] array) {
    this.array = array;
  }

  @Override
  public double get(long index) {
    return array[Math.toIntExact(index)];
  }

  public double[] getArray() {
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
    return DataType.FLOAT64;
  }

  @Override
  public boolean isGPUAccessible() {
    // Arrays are not guaranteed contiguous so a pointer isn't available to transfer to the GPU
    return false;
  }

  @Override
  public void set(long index, double value) {
    array[Math.toIntExact(index)] = value;
  }
}
