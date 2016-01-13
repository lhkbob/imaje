package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class UnsignedIntSource implements LongSource, DataView<IntSource> {
  private final IntSource source;

  public UnsignedIntSource(IntSource source) {
    this.source = source;
  }

  @Override
  public long get(long index) {
    return Integer.toUnsignedLong(source.get(index));
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public IntSource getSource() {
    return source;
  }

  @Override
  public void set(long index, long value) {
    source.set(index, (int) value);
  }
}
