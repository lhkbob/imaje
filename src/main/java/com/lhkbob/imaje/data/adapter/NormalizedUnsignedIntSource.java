package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class NormalizedUnsignedIntSource implements NumericDataSource, DataView<UnsignedIntSource> {
  private UnsignedIntSource source;

  public NormalizedUnsignedIntSource(UnsignedIntSource source) {
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
  public UnsignedIntSource getSource() {
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
    source.set(index, Math.round(UnsignedIntSource.MAX_VALUE * Functions.clamp(value, 0.0, 1.0)));
  }

  private static final double TO_DOUBLE_SCALAR = 1.0 / UnsignedIntSource.MAX_VALUE;
}
