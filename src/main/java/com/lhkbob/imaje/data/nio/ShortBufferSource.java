package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.ShortSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 *
 */
public class ShortBufferSource implements ShortSource {
  private final ShortBuffer buffer;

  public ShortBufferSource(int length) {
    this(ByteBuffer.allocateDirect(length << 1).order(ByteOrder.nativeOrder()).asShortBuffer());
  }

  public ShortBufferSource(ShortBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public short get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  public ShortBuffer getBuffer() {
    return buffer;
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
  public DataType getDataType() {
    return DataType.SINT16;
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }
}
