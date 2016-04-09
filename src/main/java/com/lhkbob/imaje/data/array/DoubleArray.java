package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class DoubleArray implements DoubleSource, DataView<double[]> {
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

  @Override
  public double[] getSource() {
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
  public void set(long index, double value) {
    array[Math.toIntExact(index)] = value;
  }
}
