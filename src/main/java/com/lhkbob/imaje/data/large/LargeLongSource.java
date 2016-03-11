package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class LargeLongSource extends AbstractLargeDataSource<Long, LongSource> implements LongSource {
  public LargeLongSource(LongSource[] sources) {
    super(sources);
  }

  @Override
  public long get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public DataType getDataType() {
    return DataType.SINT64;
  }

  @Override
  public void set(long index, long value) {
    getSource(index).set(getIndexInSource(index), value);
  }
}
