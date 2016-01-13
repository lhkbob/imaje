package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.DoubleSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

/**
 *
 */
public class DoubleBufferAdapter implements DoubleSource {
  private final DoubleBuffer buffer;

  public DoubleBufferAdapter(int length) {
    this(ByteBuffer.allocateDirect(length << 3).order(ByteOrder.nativeOrder()).asDoubleBuffer());
  }

  public DoubleBufferAdapter(DoubleBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public double get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  public DoubleBuffer getBuffer() {
    return buffer;
  }

  @Override
  public long getLength() {
    return buffer.capacity();
  }

  @Override
  public void set(long index, double value) {
    buffer.put(Math.toIntExact(index), value);
  }
}
