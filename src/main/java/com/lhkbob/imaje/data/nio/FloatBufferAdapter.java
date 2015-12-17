package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.FloatSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 *
 */
public class FloatBufferAdapter implements FloatSource {
  private final FloatBuffer buffer;

  public FloatBufferAdapter(int length) {
    this(ByteBuffer.allocateDirect(length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer());
  }

  public FloatBufferAdapter(FloatBuffer buffer) {
    this.buffer = buffer;
  }

  public FloatBuffer getBuffer() {
    return buffer;
  }

  @Override
  public long getLength() {
    return buffer.capacity();
  }

  @Override
  public float get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public void set(long index, float value) {
    buffer.put(Math.toIntExact(index), value);
  }
}
