package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 */
public class ByteBufferData implements ByteData, DataView<ByteBuffer> {
  private final ByteBuffer buffer;

  public ByteBufferData(int length) {
    this(Data.getBufferFactory().newByteBuffer(length));
  }

  public ByteBufferData(ByteBuffer buffer) {
    Arguments.notNull("buffer", buffer);
    this.buffer = buffer.duplicate();
    // Preserve the 0 -> capacity() rule for stored buffer without modifying original buffer's position and limit
    this.buffer.clear();
  }

  @Override
  public byte get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public ByteBuffer getSource() {
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
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }

  @Override
  public void set(long index, byte value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public void set(long dataIndex, byte[] values, int offset, int length) {
    // Optimize with bulk put in ByteBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ByteBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.put(values, offset, length);
    buffer.clear();
  }

  @Override
  public void set(long dataIndex, ByteBuffer values) {
    // Optimize with ByteBuffer put
    Arguments.checkArrayRange("ByteBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    buffer.put(values);
    buffer.clear();
  }

  @Override
  public void get(long dataIndex, byte[] values, int offset, int length) {
    // Optimize with bulk get in ByteBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ByteBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.get(values, offset, length);
    buffer.clear();
  }

  @Override
  public void get(long dataIndex, ByteBuffer values) {
    // Optimize with ByteBuffer put
    Arguments.checkArrayRange("ByteBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    values.put(buffer);
    buffer.clear();
  }

  private void setBufferRange(long dataIndex, int length) {
    int bufferOffset = Math.toIntExact(dataIndex);
    buffer.limit(bufferOffset + length).position(bufferOffset);
  }
}
