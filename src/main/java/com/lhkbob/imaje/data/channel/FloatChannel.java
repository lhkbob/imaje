package com.lhkbob.imaje.data.channel;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class FloatChannel extends AbstractChannel implements FloatSource, DataView<FloatSource> {
  private final FloatSource source;

  public FloatChannel(FloatSource source, long offset, long stride, long numPixels) {
    super(offset, stride, numPixels);
    this.source = source;
  }

  @Override
  public float get(long index) {
    return source.get(getSourceIndex(index));
  }

  @Override
  public FloatSource getSource() {
    return source;
  }

  @Override
  public void set(long index, float value) {
    source.set(getSourceIndex(index), value);
  }
}
