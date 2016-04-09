package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class HalfFloatSource implements NumericDataSource, DataView<ShortSource> {
  private final ShortSource source;

  public HalfFloatSource(ShortSource source) {
    this.source = source;
  }

  @Override
  public double getValue(long index) {
    return HalfFloat.halfToFloat(source.get(index));
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
  public void setValue(long index, double value) {
    source.set(index, HalfFloat.floatToHalf((float) value));
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
}
