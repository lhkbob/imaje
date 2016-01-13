package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class NormalizedUnsignedIntSource implements DoubleSource, DataView<UnsignedIntSource> {
  private UnsignedIntSource source;
  public NormalizedUnsignedIntSource(UnsignedIntSource source) {
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
  public UnsignedIntSource getSource() {
    return source;
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (int) (TO_BYTE_SCALAR * Math.max(-1.0, Math.min(value, 1.0))));
  }
  private static final double TO_BYTE_SCALAR =
      (double) Integer.MAX_VALUE - (double) Integer.MIN_VALUE;
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_BYTE_SCALAR;
}
