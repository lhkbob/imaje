package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataSources;
import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class MultiByteToDoubleSource implements DoubleSource, DataView<ByteSource> {
  private final MultiByteToLongSource source;

  public MultiByteToDoubleSource(ByteSource source, boolean bigEndian) {
    if (source.getDataType() != DataType.SINT8)
      throw new IllegalArgumentException("Source type must be SINT8 to ensure no undue bit manipulation occurs");
    this.source = new MultiByteToLongSource(source, bigEndian);
  }

  @Override
  public double get(long index) {
    return Double.longBitsToDouble(source.get(index));
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public ByteSource getSource() {
    return source.getSource();
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.FLOAT64;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible() && DataSources.isNativeEndian(this);
  }

  @Override
  public void set(long index, double value) {
    source.set(index, Double.doubleToLongBits(value));
  }
}
