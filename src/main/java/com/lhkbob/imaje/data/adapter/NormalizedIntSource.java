package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class NormalizedIntSource implements DoubleSource, DataView<IntSource> {
  private IntSource source;
  public NormalizedIntSource(IntSource source) {
    if (source.getDataType() != DataType.SINT32)
      throw new IllegalArgumentException(
          "Source type must be SINT32 to ensure no undue bit manipulation occurs");
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
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.SFIXED32;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (int) (TO_INT_SCALAR * Math.max(-1.0, Math.min(value, 1.0))));
  }
  private static final double TO_INT_SCALAR = Math.abs((double) Integer.MIN_VALUE);
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_INT_SCALAR;
}
