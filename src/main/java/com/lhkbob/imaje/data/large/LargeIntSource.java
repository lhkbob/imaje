package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class LargeIntSource extends AbstractLargeDataSource<Integer, IntSource.Primitive> implements IntSource.Primitive {
  public LargeIntSource(IntSource.Primitive[] sources) {
    super(sources);
  }

  @Override
  public int get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void set(long index, int value) {
    getSource(index).set(getIndexInSource(index), value);
  }
}
