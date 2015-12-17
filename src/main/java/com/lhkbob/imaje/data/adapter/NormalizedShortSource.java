package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class NormalizedShortSource implements DoubleSource, DataView<ShortSource> {
  private static final double TO_SHORT_SCALAR = Math.abs((double) Short.MIN_VALUE);
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_SHORT_SCALAR;

  private ShortSource source;

  public NormalizedShortSource(ShortSource source) {
    this.source = source;
  }

  @Override
  public ShortSource getSource() {
    return source;
  }

  @Override
  public double get(long index) {
    return source.get(index) * TO_DOUBLE_SCALAR;
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (short) (TO_SHORT_SCALAR * Math.max(-1.0, Math.min(value, 1.0))));
  }
}
