package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.FloatSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class HalfSource implements FloatSource, DataView<ShortSource> {
  private final ShortSource source;

  public HalfSource(ShortSource source) {
    this.source = source;
  }

  @Override
  public float get(long index) {
    return halfToFloat(source.get(index));
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
    source.set(index, floatToHalf(value));
  }

  private static short floatToHalf(float value) {
    return HalfFloat.floatToHalf(value);
  }

  private static float halfToFloat(short halfValue) {
    return HalfFloat.halfToFloat(halfValue);
  }
}
