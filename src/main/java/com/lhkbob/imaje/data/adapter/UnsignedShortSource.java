package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class UnsignedShortSource implements IntSource, DataView<ShortSource.Primitive> {
  public static final int MAX_VALUE = Short.MAX_VALUE - Short.MIN_VALUE;
  private final ShortSource.Primitive source;

  public UnsignedShortSource(ShortSource.Primitive source) {
    this.source = source;
  }

  @Override
  public int get(long index) {
    return Short.toUnsignedInt(source.get(index));
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
  public ShortSource.Primitive getSource() {
    return source;
  }

  @Override
  public void set(long index, int value) {
    // Clamp to unsigned byte boundaries
    value = Math.max(0, Math.min(value, MAX_VALUE));
    source.set(index, (short) value);
  }
}
