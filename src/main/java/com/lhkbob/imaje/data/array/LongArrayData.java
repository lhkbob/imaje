package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.LongBuffer;

/**
 *
 */
public class LongArrayData implements LongData, DataView<long[]> {
  private final long[] array;

  public LongArrayData(int length) {
    this(new long[length]);
  }

  public LongArrayData(long[] array) {
    Arguments.notNull("array", array);
    this.array = array;
  }

  @Override
  public long get(long index) {
    return array[Math.toIntExact(index)];
  }

  @Override
  public long[] getSource() {
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
  public void set(long index, long value) {
    array[Math.toIntExact(index)] = value;
  }

  @Override
  public void set(long dataIndex, long[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LongArrayData", getLength(), dataIndex, length);

    System.arraycopy(values, offset, array, Math.toIntExact(dataIndex), length);
  }

  @Override
  public void set(long dataIndex, LongBuffer values) {
    // Optimize with bulk get defined in LongBuffer
    Arguments.checkArrayRange("ByteArrayData", getLength(), dataIndex, values.remaining());
    values.get(array, Math.toIntExact(dataIndex), values.remaining());
  }

  @Override
  public void get(long dataIndex, long[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LongArrayData", getLength(), dataIndex, length);

    System.arraycopy(array, Math.toIntExact(dataIndex), values, offset, length);
  }

  @Override
  public void get(long dataIndex, LongBuffer values) {
    // Optimize with bulk put defined in LongBuffer
    Arguments.checkArrayRange("LongArrayData", getLength(), dataIndex, values.remaining());
    values.put(array, Math.toIntExact(dataIndex), values.remaining());
  }
}