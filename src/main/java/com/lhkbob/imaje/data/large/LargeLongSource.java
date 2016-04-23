package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class LargeLongSource extends AbstractLargeDataSource<LongSource> implements LongSource {
  public LargeLongSource(LongSource[] sources) {
    super(sources);
  }

  @Override
  public long get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void set(long index, long value) {
    getSource(index).set(getIndexInSource(index), value);
  }
}
