package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.ByteSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 */
public class ByteBufferSource implements ByteSource.Primitive {
  private final ByteBuffer buffer;

  public ByteBufferSource(int length) {
    this(ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder()));
  }

  public ByteBufferSource(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public byte get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  public ByteBuffer getBuffer() {
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
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }

  @Override
  public void set(long index, byte value) {
    buffer.put(Math.toIntExact(index), value);
  }
}
