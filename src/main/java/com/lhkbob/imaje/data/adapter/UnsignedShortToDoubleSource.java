package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class UnsignedShortToDoubleSource implements DoubleSource, DataView<UnsignedShortSource> {
  private final UnsignedShortSource source;

  public UnsignedShortToDoubleSource(UnsignedShortSource source) {
    this.source = source;
  }

  @Override
  public double get(long index) {
    // The unsigned short, lifted to an int, is automatically converted to a double correctly
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
    // Clamp value to be between the minimum and maximum unsigned byte values
    value = Math.max(0, Math.min(value, UnsignedShortSource.MAX_VALUE));
    source.set(index, (int) value);
  }

  @Override
  public UnsignedShortSource getSource() {
    return source;
  }
}
