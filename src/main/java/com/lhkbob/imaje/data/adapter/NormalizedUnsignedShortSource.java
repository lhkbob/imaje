package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class NormalizedUnsignedShortSource implements NumericDataSource, DataView<UnsignedShortSource> {
  private UnsignedShortSource source;

  public NormalizedUnsignedShortSource(UnsignedShortSource source) {
    this.source = source;
  }

  @Override
  public double getValue(long index) {
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
  public int getBitSize() {
    return source.getBitSize();
  }

  @Override
  public void setValue(long index, double value) {
    source.set(index,
        (int) Math.round(UnsignedShortSource.MAX_VALUE * Functions.clamp(value, 0.0, 1.0)));
  }

  private static final double TO_DOUBLE_SCALAR = 1.0 / UnsignedShortSource.MAX_VALUE;
}
