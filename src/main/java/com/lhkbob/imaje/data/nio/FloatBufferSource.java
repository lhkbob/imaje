package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.FloatSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 *
 */
public class FloatBufferSource implements FloatSource.Primitive {
  private final FloatBuffer buffer;

  public FloatBufferSource(int length) {
    this(ByteBuffer.allocateDirect(length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer());
  }

  public FloatBufferSource(FloatBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public float get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  public FloatBuffer getBuffer() {
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
  public void set(long index, float value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }
}
