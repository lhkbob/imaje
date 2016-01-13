package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class NormalizedIntSource implements DoubleSource, DataView<IntSource> {
  private IntSource source;
  public NormalizedIntSource(IntSource source) {
    this.source = source;
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
  public IntSource getSource() {
    return source;
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (int) (TO_INT_SCALAR * Math.max(-1.0, Math.min(value, 1.0))));
  }
  private static final double TO_INT_SCALAR = Math.abs((double) Integer.MIN_VALUE);
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_INT_SCALAR;
}
