package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class NormalizedLongSource implements DoubleSource, DataView<LongSource> {
  private static final double TO_LONG_SCALAR = Math.abs((double) Long.MIN_VALUE);
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_LONG_SCALAR;

  private LongSource source;

  public NormalizedLongSource(LongSource source) {
    this.source = source;
  }

  @Override
  public LongSource getSource() {
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
    source.set(index, (long) (TO_LONG_SCALAR * Math.max(-1.0, Math.min(value, 1.0))));
  }
}
