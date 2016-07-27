package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteBuffer;

/**
 *
 */
public class ByteArrayData implements ByteData, DataView<byte[]> {
  private final byte[] array;

  public ByteArrayData(int length) {
    this(new byte[length]);
  }

  public ByteArrayData(byte[] array) {
    Arguments.notNull("array", array);
    this.array = array;
  }

  @Override
  public byte get(long index) {
    return array[Math.toIntExact(index)];
  }

  @Override
  public byte[] getSource() {
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

  @Override
  public void set(long dataIndex, byte[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ByteArrayData", getLength(), dataIndex, length);

    System.arraycopy(values, offset, array, Math.toIntExact(dataIndex), length);
  }

  @Override
  public void set(long dataIndex, ByteBuffer values, int offset, int length) {
    // Optimize with bulk get defined in ByteBuffer
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("ByteArrayData", getLength(), dataIndex, length);

    // Preserve buffer state since we have to manipulate position when making bulk get call
    int oldPos = values.position();
    int oldLimit = values.limit();

    values.limit(offset + length).position(offset);
    values.get(array, Math.toIntExact(dataIndex), length);

    // Restore buffer state
    values.limit(oldLimit).position(oldPos);
  }

  @Override
  public void get(long dataIndex, byte[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ByteArrayData", getLength(), dataIndex, length);

    System.arraycopy(array, Math.toIntExact(dataIndex), values, offset, length);
  }

  @Override
  public void get(long dataIndex, ByteBuffer values, int offset, int length) {
    // Optimize with bulk put defined in IntBuffer
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("ByteArrayData", getLength(), dataIndex, length);

    // Preserve buffer state since we have to manipulate position when making bulk get call
    int oldPos = values.position();
    int oldLimit = values.limit();

    values.limit(offset + length).position(offset);
    values.put(array, Math.toIntExact(dataIndex), length);

    // Restore buffer state
    values.limit(oldLimit).position(oldPos);
  }
}
