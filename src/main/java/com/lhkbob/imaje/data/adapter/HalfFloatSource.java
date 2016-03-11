package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.FloatSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class HalfFloatSource implements FloatSource, DataView<ShortSource> {
  private final ShortSource source;

  public HalfFloatSource(ShortSource source) {
    if (source.getDataType() != DataType.SINT16) {
      throw new IllegalArgumentException(
          "HalfFloatSource requires the ShortSource to be type SINT16 to ensure that no undue manipulation of the bits is being performed");
    }
    this.source = source;
  }

  @Override
  public float get(long index) {
    return HalfFloat.halfToFloat(source.get(index));
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public ShortSource getSource() {
    return source;
  }

  @Override
  public void set(long index, float value) {
    source.set(index, HalfFloat.floatToHalf(value));
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.FLOAT16;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }
}
