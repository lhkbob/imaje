package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.ShortSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 *
 */
public class ShortBufferAdapter implements ShortSource {
  private final ShortBuffer buffer;

  public ShortBufferAdapter(int length) {
    this(ByteBuffer.allocateDirect(length << 1).order(ByteOrder.nativeOrder()).asShortBuffer());
  }

  public ShortBufferAdapter(ShortBuffer buffer) {
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
  public void set(long index, short value) {
    buffer.put(Math.toIntExact(index), value);
  }
}
