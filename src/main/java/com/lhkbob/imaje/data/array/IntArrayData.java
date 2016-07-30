package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.IntBuffer;

/**
 *
 */
public class IntArrayData implements IntData, DataView<int[]> {
  private final int[] array;

  public IntArrayData(int length) {
    this(new int[length]);
  }

  public IntArrayData(int[] array) {
    Arguments.notNull("array", array);
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

  @Override
  public void set(long dataIndex, int[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("IntArrayData", getLength(), dataIndex, length);

    System.arraycopy(values, offset, array, Math.toIntExact(dataIndex), length);
  }

  @Override
  public void set(long dataIndex, IntBuffer values) {
    // Optimize with bulk get defined in IntBuffer
    Arguments.checkArrayRange("IntArrayData", getLength(), dataIndex, values.remaining());
    values.get(array, Math.toIntExact(dataIndex), values.remaining());
  }

  @Override
  public void get(long dataIndex, int[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("IntArrayData", getLength(), dataIndex, length);

    System.arraycopy(array, Math.toIntExact(dataIndex), values, offset, length);
  }

  @Override
  public void get(long dataIndex, IntBuffer values) {
    // Optimize with bulk put defined in IntBuffer
    Arguments.checkArrayRange("IntArrayData", getLength(), dataIndex, values.remaining());
    values.put(array, Math.toIntExact(dataIndex), values.remaining());
  }
}
