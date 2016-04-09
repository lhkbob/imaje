package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class NormalizedIntSource implements NumericDataSource, DataView<IntSource> {
  private IntSource source;
  public NormalizedIntSource(IntSource source) {
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
  public IntSource getSource() {
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
    source.set(index, (int) Math.round(TO_INT_SCALAR * Functions
        .clamp(value, -1.0, 1.0)));
  }
  private static final double TO_INT_SCALAR = Integer.MAX_VALUE;
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_INT_SCALAR;
}
