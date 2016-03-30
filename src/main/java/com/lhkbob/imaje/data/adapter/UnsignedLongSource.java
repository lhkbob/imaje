package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class UnsignedLongSource implements LongSource, DataView<LongSource.Primitive> {
  private final LongSource.Primitive source;

  public UnsignedLongSource(LongSource.Primitive source) {
    this.source = source;
  }

  @Override
  public long get(long index) {
    return source.get(index);
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public void set(long index, long value) {
    source.set(index, value);
  }

  @Override
  public LongSource.Primitive getSource() {
    return source;
  }
}
