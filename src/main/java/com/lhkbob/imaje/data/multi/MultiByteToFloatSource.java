package com.lhkbob.imaje.data.multi;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class MultiByteToFloatSource implements FloatSource, DataView<ByteSource> {
  private final MultiByteToIntSource source;

  public MultiByteToFloatSource(ByteSource source, boolean bigEndian) {
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
  public void set(long index, float value) {
    source.set(index, Float.floatToIntBits(value));
  }
}