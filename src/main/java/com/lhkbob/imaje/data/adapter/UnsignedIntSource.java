package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class UnsignedIntSource implements NumericDataSource, DataView<IntSource> {
  public static final long MAX_VALUE = (long) Integer.MAX_VALUE - (long) Integer.MIN_VALUE;

  private final IntSource source;

  public UnsignedIntSource(IntSource source) {
    this.source = source;
  }

  public long get(long index) {
    return Integer.toUnsignedLong(source.get(index));
  }

  @Override
  public double getValue(long index) {
    return get(index);
  }

  @Override
  public void setValue(long index, double value) {
    // Clamp to unsigned byte boundaries
    value = Functions.clamp(value, 0, MAX_VALUE);
    source.set(index, (int) Math.round(value));
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
  public int getBitSize() {
    return source.getBitSize();
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public IntSource getSource() {
    return source;
  }

  public void set(long index, long value) {
    // Clamp to unsigned byte boundaries
    value = Functions.clamp(value, 0, MAX_VALUE);
    source.set(index, (int) value);
  }
}
