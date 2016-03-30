package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class LargeFloatSource extends AbstractLargeDataSource<Float, FloatSource.Primitive> implements FloatSource.Primitive {
  public LargeFloatSource(FloatSource.Primitive[] sources) {
    super(sources);
  }

  @Override
  public float get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void set(long index, float value) {
    getSource(index).set(getIndexInSource(index), value);
  }
}
