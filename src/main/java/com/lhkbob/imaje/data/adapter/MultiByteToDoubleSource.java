package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataSources;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class MultiByteToDoubleSource implements DoubleSource.Primitive, DataView<ByteSource.Primitive> {
  private final MultiByteToLongSource source;

  public MultiByteToDoubleSource(ByteSource.Primitive source, boolean bigEndian) {
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
  public ByteSource.Primitive getSource() {
    return source.getSource();
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
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
