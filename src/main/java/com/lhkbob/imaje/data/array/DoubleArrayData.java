package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.DoubleBuffer;

/**
 *
 */
public class DoubleArrayData implements DoubleData, DataView<double[]> {
  private final double[] array;

  public DoubleArrayData(int length) {
    this(new double[length]);
  }

  public DoubleArrayData(double[] array) {
    Arguments.notNull("array", array);
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

  @Override
  public void setValues(long dataIndex, double[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("DoubleArrayData", getLength(), dataIndex, length);

    System.arraycopy(values, offset, array, Math.toIntExact(dataIndex), length);
  }

  @Override
  public void setValues(long dataIndex, DoubleBuffer values) {
    // Optimize with bulk get defined in DoubleBuffer
    Arguments.checkArrayRange("DoubleArrayData", getLength(), dataIndex, values.remaining());
    values.get(array, Math.toIntExact(dataIndex), values.remaining());
  }

  @Override
  public void getValues(long dataIndex, double[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("DoubleArrayData", getLength(), dataIndex, length);

    System.arraycopy(array, Math.toIntExact(dataIndex), values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, DoubleBuffer values) {
    // Optimize with bulk put defined in DoubleBuffer
    Arguments.checkArrayRange("DoubleArrayData", getLength(), dataIndex, values.remaining());
    values.put(array, Math.toIntExact(dataIndex), values.remaining());
  }
}
