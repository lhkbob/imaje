package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 *
 */
public class ShortBufferData implements ShortData, DataView<ShortBuffer> {
  private final ShortBuffer buffer;

  public ShortBufferData(int length) {
    this(Data.getBufferFactory().newShortBuffer(length));
  }

  public ShortBufferData(ShortBuffer buffer) {
    Arguments.notNull("buffer", buffer);
    this.buffer = buffer.duplicate();
    // Preserve the 0 -> capacity() rule for stored buffer without modifying original buffer's position and limit
    this.buffer.clear();
  }

  @Override
  public short get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public ShortBuffer getSource() {
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
  public void set(long index, short value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }

  @Override
  public void set(long dataIndex, short[] values, int offset, int length) {
    // Optimize with bulk put in ShortBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.put(values, offset, length);
    buffer.clear();
  }

  @Override
  public void set(long dataIndex, ShortBuffer values) {
    // Optimize with ShortBuffer put
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    buffer.put(values);
    buffer.clear();
  }

  @Override
  public void get(long dataIndex, short[] values, int offset, int length) {
    // Optimize with bulk get in ShortBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.get(values, offset, length);
    buffer.clear();
  }

  @Override
  public void get(long dataIndex, ShortBuffer values) {
    // Optimize with ShortBuffer put
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    values.put(buffer);
    buffer.clear();
  }

  private void setBufferRange(long dataIndex, int length) {
    int bufferOffset = Math.toIntExact(dataIndex);
    buffer.limit(bufferOffset + length).position(bufferOffset);
  }
}
