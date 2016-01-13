package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.IntSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 *
 */
public class IntBufferAdapter implements IntSource {
  private final IntBuffer buffer;

  public IntBufferAdapter(int length) {
    this(ByteBuffer.allocateDirect(length << 2).order(ByteOrder.nativeOrder()).asIntBuffer());
  }

  public IntBufferAdapter(IntBuffer buffer) {
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
  public void set(long index, int value) {
    buffer.put(Math.toIntExact(index), value);
  }
}
