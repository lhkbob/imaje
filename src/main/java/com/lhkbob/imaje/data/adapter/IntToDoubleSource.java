package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class IntToDoubleSource implements DoubleSource, DataView<IntSource.Primitive> {
  private final IntSource.Primitive source;

  public IntToDoubleSource(IntSource.Primitive source) {
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
    // Clamp to the range of int values so casting roll-over isn't so surprising
    value = Math.max(Integer.MIN_VALUE, Math.min(value, Integer.MAX_VALUE));
    source.set(index, (int) value);
  }

  @Override
  public IntSource.Primitive getSource() {
    return source;
  }
}
