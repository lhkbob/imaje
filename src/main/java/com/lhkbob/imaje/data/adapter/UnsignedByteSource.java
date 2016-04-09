package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class UnsignedByteSource implements NumericDataSource, DataView<ByteSource> {
  public static final int MAX_VALUE = Byte.MAX_VALUE - Byte.MIN_VALUE;

  private final ByteSource source;

  public UnsignedByteSource(ByteSource source) {
    this.source = source;
  }

  public int get(long index) {
    return Byte.toUnsignedInt(source.get(index));
  }

  @Override
  public double getValue(long index) {
    return get(index);
  }

  @Override
  public void setValue(long index, double value) {
    // Clamp to unsigned byte boundaries
    value = Functions.clamp(value, 0.0, MAX_VALUE);
    source.set(index, (byte) Math.round(value));
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
  public int getBitSize() {
    return source.getBitSize();
  }

  @Override
  public ByteSource getSource() {
    return source;
  }

  public void set(long index, int value) {
    // Clamp to unsigned byte boundaries
    value = Functions.clamp(value, 0, MAX_VALUE);
    source.set(index, (byte) value);
  }
}
