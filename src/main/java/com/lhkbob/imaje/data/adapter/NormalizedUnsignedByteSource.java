package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class NormalizedUnsignedByteSource implements DoubleSource, DataView<UnsignedByteSource> {
  private UnsignedByteSource source;
  public NormalizedUnsignedByteSource(UnsignedByteSource source) {
    this.source = source;
  }

  @Override
  public double get(long index) {
    return source.get(index) * TO_DOUBLE_SCALAR;
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public UnsignedByteSource getSource() {
    return source;
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.UFIXED8;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (int) (TO_BYTE_SCALAR * Math.max(0.0, Math.min(value, 1.0))));
  }
  private static final double TO_BYTE_SCALAR = (double) Byte.MAX_VALUE - (double) Byte.MIN_VALUE;
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_BYTE_SCALAR;
}
