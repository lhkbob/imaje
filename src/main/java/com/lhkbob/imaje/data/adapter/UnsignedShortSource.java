package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.data.ShortSource;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class UnsignedShortSource implements NumericDataSource, DataView<ShortSource> {
  public static final int MAX_VALUE = Short.MAX_VALUE - Short.MIN_VALUE;
  private final ShortSource source;

  public UnsignedShortSource(ShortSource source) {
    this.source = source;
  }

  public int get(long index) {
    return Short.toUnsignedInt(source.get(index));
  }

  @Override
  public double getValue(long index) {
    return get(index);
  }

  @Override
  public void setValue(long index, double value) {
    // Clamp to unsigned byte boundaries
    value = Functions.clamp(value, 0, MAX_VALUE);
    source.set(index, (short) Math.round(value));
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

  public ShortSource getSource() {
    return source;
  }

  public void set(long index, int value) {
    // Clamp to unsigned byte boundaries
    value = Functions.clamp(value, 0, MAX_VALUE);
    source.set(index, (short) value);
  }
}
