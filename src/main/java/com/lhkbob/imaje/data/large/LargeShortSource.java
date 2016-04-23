package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class LargeShortSource extends AbstractLargeDataSource<ShortSource> implements ShortSource {
  public LargeShortSource(ShortSource[] sources) {
    super(sources);
  }

  @Override
  public short get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void set(long index, short value) {
    getSource(index).set(getIndexInSource(index), value);
  }
}
