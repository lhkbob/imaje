package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class UnsignedByteSource implements IntSource, DataView<ByteSource.Primitive> {
  public static final int MAX_VALUE = Byte.MAX_VALUE - Byte.MIN_VALUE;

  private final ByteSource.Primitive source;

  public UnsignedByteSource(ByteSource.Primitive source) {
    this.source = source;
  }

  @Override
  public int get(long index) {
    return (short) Byte.toUnsignedInt(source.get(index));
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public ByteSource.Primitive getSource() {
    return source;
  }

  @Override
  public void set(long index, int value) {
    // Clamp to unsigned byte boundaries
    value = Math.max(0, Math.min(value, MAX_VALUE));
    source.set(index, (byte) value);
  }
}
