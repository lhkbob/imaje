package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.ByteSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 */
public class ByteBufferAdapter implements ByteSource {
  private final ByteBuffer buffer;

  public ByteBufferAdapter(int length) {
    this(ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder()));
  }

  public ByteBufferAdapter(ByteBuffer buffer) {
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
  public void set(long index, byte value) {
    buffer.put(Math.toIntExact(index), value);
  }
}
