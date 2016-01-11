package com.lhkbob.imaje.data.channel;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class DoubleChannel extends AbstractChannel implements DoubleSource, DataView<DoubleSource> {
  private final DoubleSource source;

  public DoubleChannel(DoubleSource source, long offset, long stride, long numPixels) {
    super(offset, stride, numPixels);
    this.source = source;
  }

  @Override
  public double get(long index) {
    return source.get(getSourceIndex(index));
  }

  @Override
  public DoubleSource getSource() {
    return source;
  }

  @Override
  public void set(long index, double value) {
    source.set(getSourceIndex(index), value);
  }
}
