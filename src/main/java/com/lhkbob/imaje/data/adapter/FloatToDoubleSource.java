package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class FloatToDoubleSource implements DoubleSource, DataView<FloatSource> {
  private final FloatSource source;

  public FloatToDoubleSource(FloatSource source) {
    this.source = source;
  }

  @Override
  public FloatSource getSource() {
    return source;
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public double get(long index) {
    return source.get(index);
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (float) value);
  }
}
