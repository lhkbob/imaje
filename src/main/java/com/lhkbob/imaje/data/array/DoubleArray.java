package com.lhkbob.imaje.data.array;

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

  public double[] getArray() {
    return array;
  }

  @Override
  public long getLength() {
    return array.length;
  }

  @Override
  public double get(long index) {
    return array[Math.toIntExact(index)];
  }

  @Override
  public void set(long index, double value) {
    array[Math.toIntExact(index)] = value;
  }
}
