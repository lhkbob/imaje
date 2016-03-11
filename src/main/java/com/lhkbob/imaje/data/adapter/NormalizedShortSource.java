package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class NormalizedShortSource implements DoubleSource, DataView<ShortSource> {
  private ShortSource source;
  public NormalizedShortSource(ShortSource source) {
    if (source.getDataType() != DataType.SINT16)
      throw new IllegalArgumentException(
          "Source type must be SINT16 to ensure no undue bit manipulation occurs");
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
  public ShortSource getSource() {
    return source;
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.SFIXED16;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (short) (TO_SHORT_SCALAR * Math.max(-1.0, Math.min(value, 1.0))));
  }
  private static final double TO_SHORT_SCALAR = Math.abs((double) Short.MIN_VALUE);
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_SHORT_SCALAR;
}
