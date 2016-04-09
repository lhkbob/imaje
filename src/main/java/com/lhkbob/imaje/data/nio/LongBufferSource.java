package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.LongSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

/**
 *
 */
public class LongBufferSource implements LongSource, DataView<LongBuffer> {
  private final LongBuffer buffer;

  public LongBufferSource(int length) {
    this(ByteBuffer.allocateDirect(length << 3).order(ByteOrder.nativeOrder()).asLongBuffer());
  }

  public LongBufferSource(LongBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public long get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public LongBuffer getSource() {
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
  public void set(long index, long value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }
}
