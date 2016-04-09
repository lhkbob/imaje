package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class MultiByteToDoubleSource implements DoubleSource, DataView<ByteSource> {
  private final MultiByteToLongSource source;

  public MultiByteToDoubleSource(ByteSource source, boolean bigEndian) {
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
  public boolean isGPUAccessible() {
    return source.isGPUAccessible() && Data.isNativeEndian(this);
  }

  @Override
  public void set(long index, double value) {
    source.set(index, Double.doubleToLongBits(value));
  }
}
