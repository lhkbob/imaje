package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class NormalizedUnsignedShortSource implements DoubleSource, DataView<UnsignedShortSource> {
  private UnsignedShortSource source;
  public NormalizedUnsignedShortSource(UnsignedShortSource source) {
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
  public UnsignedShortSource getSource() {
    return source;
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
    source.set(index, (int) (UnsignedShortSource.MAX_VALUE * Math.max(0.0, Math.min(value, 1.0))));
  }
  private static final double TO_DOUBLE_SCALAR = 1.0 / UnsignedShortSource.MAX_VALUE;
}
