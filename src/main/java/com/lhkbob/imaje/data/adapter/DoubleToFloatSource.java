package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class DoubleToFloatSource implements FloatSource, DataView<DoubleSource> {
  private final DoubleSource source;

  public DoubleToFloatSource(DoubleSource source) {
    this.source = source;
  }

  @Override
  public DoubleSource getSource() {
    return source;
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public float get(long index) {
    return (float) source.get(index);
  }

  @Override
  public void set(long index, float value) {
    source.set(index, value);
  }
}
