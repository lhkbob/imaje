package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class ByteToDoubleSource implements DoubleSource, DataView<ByteSource.Primitive> {
  private final ByteSource.Primitive source;

  public ByteToDoubleSource(ByteSource.Primitive source) {
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
    // Clamp to the range of byte values so casting outside of the byte range isn't quite so surprising
    value = Math.max(Byte.MIN_VALUE, Math.min(value, Byte.MAX_VALUE));
    source.set(index, (byte) value);
  }

  @Override
  public ByteSource.Primitive getSource() {
    return source;
  }
}
