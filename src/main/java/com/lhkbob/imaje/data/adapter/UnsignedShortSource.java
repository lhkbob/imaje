package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class UnsignedShortSource implements IntSource, DataView<ShortSource> {
  private final ShortSource source;

  public UnsignedShortSource(ShortSource source) {
    this.source = source;
  }

  @Override
  public int get(long index) {
    return Short.toUnsignedInt(source.get(index));
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public ShortSource getSource() {
    return source;
  }

  @Override
  public void set(long index, int value) {
    source.set(index, (short) value);
  }
}
