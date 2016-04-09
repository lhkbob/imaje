package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.ShortSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 *
 */
public class ShortBufferSource implements ShortSource, DataView<ShortBuffer> {
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

  @Override
  public ShortBuffer getSource() {
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
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }
}
