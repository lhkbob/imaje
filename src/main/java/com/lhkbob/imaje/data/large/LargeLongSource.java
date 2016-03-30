package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class LargeLongSource extends AbstractLargeDataSource<Long, LongSource.Primitive> implements LongSource.Primitive {
  public LargeLongSource(LongSource.Primitive[] sources) {
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
