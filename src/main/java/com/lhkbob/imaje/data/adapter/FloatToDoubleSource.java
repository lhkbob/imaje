package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class FloatToDoubleSource implements DoubleSource, DataView<FloatSource.Primitive> {
  private final FloatSource.Primitive source;

  public FloatToDoubleSource(FloatSource.Primitive source) {
    this.source = source;
  }

  @Override
  public double get(long index) {
    return source.get(index);
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
  public long getLength() {
    return source.getLength();
  }

  @Override
  public FloatSource.Primitive getSource() {
    return source;
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (float) value);
  }
}
