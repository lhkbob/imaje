package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class ShortToDoubleSource implements DoubleSource, DataView<ShortSource.Primitive> {
  private final ShortSource.Primitive source;

  public ShortToDoubleSource(ShortSource.Primitive source) {
    this.source = source;
  }

  @Override
  public double get(long index) {
    return source.get(index);
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
  public void set(long index, double value) {
    // Clamp to short boundaries so roll-over on casting is not surprising
    value = Math.max(Short.MIN_VALUE, Math.min(value, Short.MAX_VALUE));
    source.set(index, (short) value);
  }

  @Override
  public ShortSource.Primitive getSource() {
    return source;
  }
}
