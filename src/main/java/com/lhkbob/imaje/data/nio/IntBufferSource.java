package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.IntSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 *
 */
public class IntBufferSource implements IntSource.Primitive {
  private final IntBuffer buffer;

  public IntBufferSource(int length) {
    this(ByteBuffer.allocateDirect(length << 2).order(ByteOrder.nativeOrder()).asIntBuffer());
  }

  public IntBufferSource(IntBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public int get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  public IntBuffer getBuffer() {
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
  public void set(long index, int value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }
}
