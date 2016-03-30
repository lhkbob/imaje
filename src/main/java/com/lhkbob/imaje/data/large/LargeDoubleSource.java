package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class LargeDoubleSource extends AbstractLargeDataSource<Double, DoubleSource.Primitive> implements DoubleSource.Primitive {
  public LargeDoubleSource(DoubleSource.Primitive[] sources) {
    super(sources);
  }

  @Override
  public double get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void set(long index, double value) {
    getSource(index).set(getIndexInSource(index), value);
  }
}
