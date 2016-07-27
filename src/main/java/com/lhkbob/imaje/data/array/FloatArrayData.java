package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.FloatBuffer;

/**
 *
 */
public class FloatArrayData implements FloatData, DataView<float[]> {
  private final float[] array;

  public FloatArrayData(int length) {
    this(new float[length]);
  }

  public FloatArrayData(float[] array) {
    Arguments.notNull("array", array);
    this.array = array;
  }

  @Override
  public float get(long index) {
    return array[Math.toIntExact(index)];
  }

  @Override
  public float[] getSource() {
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
  public void set(long index, float value) {
    array[Math.toIntExact(index)] = value;
  }

  @Override
  public void setValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("FloatArrayData", getLength(), dataIndex, length);

    System.arraycopy(values, offset, array, Math.toIntExact(dataIndex), length);
  }

  @Override
  public void setValues(long dataIndex, FloatBuffer values, int offset, int length) {
    // Optimize with bulk get defined in FloatBuffer
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("FloatArrayData", getLength(), dataIndex, length);

    // Preserve buffer state since we have to manipulate position when making bulk get call
    int oldPos = values.position();
    int oldLimit = values.limit();

    values.limit(offset + length).position(offset);
    values.get(array, Math.toIntExact(dataIndex), length);

    // Restore buffer state
    values.limit(oldLimit).position(oldPos);
  }

  @Override
  public void getValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("FloatArrayData", getLength(), dataIndex, length);

    System.arraycopy(array, Math.toIntExact(dataIndex), values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, FloatBuffer values, int offset, int length) {
    // Optimize with bulk put defined in FloatBuffer
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("FloatArrayData", getLength(), dataIndex, length);

    // Preserve buffer state since we have to manipulate position when making bulk get call
    int oldPos = values.position();
    int oldLimit = values.limit();

    values.limit(offset + length).position(offset);
    values.put(array, Math.toIntExact(dataIndex), length);

    // Restore buffer state
    values.limit(oldLimit).position(oldPos);
  }
}
