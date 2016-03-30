package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class UnsignedIntSource implements LongSource, DataView<IntSource.Primitive> {
  public static final long MAX_VALUE = (long) Integer.MAX_VALUE - (long) Integer.MIN_VALUE;

  private final IntSource.Primitive source;

  public UnsignedIntSource(IntSource.Primitive source) {
    this.source = source;
  }

  @Override
  public long get(long index) {
    return Integer.toUnsignedLong(source.get(index));
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
  public long getLength() {
    return source.getLength();
  }

  @Override
  public IntSource.Primitive getSource() {
    return source;
  }

  @Override
  public void set(long index, long value) {
    // Clamp to unsigned byte boundaries
    value = Math.max(0, Math.min(value, MAX_VALUE));
    source.set(index, (int) value);
  }
}
