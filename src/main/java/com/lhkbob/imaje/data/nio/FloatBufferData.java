package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 *
 */
public class FloatBufferData implements FloatData, DataView<FloatBuffer> {
  private final FloatBuffer buffer;

  public FloatBufferData(int length) {
    this(Data.getBufferFactory().newFloatBuffer(length));
  }

  public FloatBufferData(FloatBuffer buffer) {
    Arguments.notNull("buffer", buffer);
    this.buffer = buffer.duplicate();
    // Preserve the 0 -> capacity() rule for stored buffer without modifying original buffer's position and limit
    this.buffer.clear();
  }

  @Override
  public float get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public FloatBuffer getSource() {
    return buffer.duplicate();
  }

  @Override
  public long getLength() {
    return buffer.capacity();
  }

  @Override
  public boolean isBigEndian() {
    return buffer.order().equals(ByteOrder.BIG_ENDIAN);
  }

  @Override
  public void set(long index, float value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }

  @Override
  public void setValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize with bulk put in FloatBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("FloatBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.put(values, offset, length);
    buffer.clear();
  }

  @Override
  public void setValues(long dataIndex, FloatBuffer values) {
    // Optimize with FloatBuffer put
    Arguments.checkArrayRange("FloatBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    buffer.put(values);
    buffer.clear();
  }

  @Override
  public void getValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize with bulk get in FloatBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("FloatBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.get(values, offset, length);
    buffer.clear();
  }

  @Override
  public void getValues(long dataIndex, FloatBuffer values) {
    // Optimize with FloatBuffer put
    Arguments.checkArrayRange("FloatBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    values.put(buffer);
    buffer.clear();
  }

  private void setBufferRange(long dataIndex, int length) {
    int bufferOffset = Math.toIntExact(dataIndex);
    buffer.limit(bufferOffset + length).position(bufferOffset);
  }
}
