package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.LongSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

/**
 *
 */
public class LongBufferAdapter implements LongSource {
  private final LongBuffer buffer;

  public LongBufferAdapter(int length) {
    this(ByteBuffer.allocateDirect(length << 3).order(ByteOrder.nativeOrder()).asLongBuffer());
  }

  public LongBufferAdapter(LongBuffer buffer) {
    this.buffer = buffer;
  }

  public LongBuffer getBuffer() {
    return buffer;
  }

  @Override
  public long getLength() {
    return buffer.capacity();
  }

  @Override
  public long get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public void set(long index, long value) {
    buffer.put(Math.toIntExact(index), value);
  }
}
