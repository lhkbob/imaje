package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class NormalizedByteSource implements DoubleSource, DataView<ByteSource> {
  private ByteSource source;
  public NormalizedByteSource(ByteSource source) {
    if (source.getDataType() != DataType.SINT8)
      throw new IllegalArgumentException(
          "Source type must be SINT8 to ensure no undue bit manipulation occurs");
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
  public ByteSource getSource() {
    return source;
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.SFIXED8;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public void set(long index, double value) {
    source.set(index, (byte) (TO_BYTE_SCALAR * Math.max(-1.0, Math.min(value, 1.0))));
  }
  private static final double TO_BYTE_SCALAR = Math.abs((double) Byte.MIN_VALUE);
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_BYTE_SCALAR;
}
