package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class LargeIntSource extends AbstractLargeDataSource<IntSource> implements IntSource {
  public LargeIntSource(IntSource[] sources) {
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
