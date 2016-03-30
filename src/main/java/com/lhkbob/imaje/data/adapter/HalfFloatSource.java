package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class HalfFloatSource implements DoubleSource, DataView<ShortSource.Primitive> {
  private final ShortSource.Primitive source;

  public HalfFloatSource(ShortSource.Primitive source) {
    this.source = source;
  }

  @Override
  public double get(long index) {
    return HalfFloat.halfToFloat(source.get(index));
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public ShortSource.Primitive getSource() {
    return source;
  }

  @Override
  public void set(long index, double value) {
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
}
