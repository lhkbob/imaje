package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataSources;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class MultiByteToFloatSource implements FloatSource.Primitive, DataView<ByteSource.Primitive> {
  private final MultiByteToIntSource source;

  public MultiByteToFloatSource(ByteSource.Primitive source, boolean bigEndian) {
    this.source = new MultiByteToIntSource(source, bigEndian);
  }

  @Override
  public float get(long index) {
    return Float.intBitsToFloat(source.get(index));
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
  public void set(long index, float value) {
    source.set(index, Float.floatToIntBits(value));
  }
}
