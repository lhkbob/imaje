package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class LongToDoubleSource implements DoubleSource, DataView<LongSource.Primitive> {
  private final LongSource.Primitive source;

  public LongToDoubleSource(LongSource.Primitive source) {
    this.source = source;
  }

  @Override
  public double get(long index) {
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
  public void set(long index, double value) {
    // Clamp to long boundary values so casting roll-over from double isn't surprising
    value = Math.max(Long.MIN_VALUE, Math.min(value, Long.MAX_VALUE));
    source.set(index, (long) value);
  }

  @Override
  public LongSource.Primitive getSource() {
    return source;
  }
}
