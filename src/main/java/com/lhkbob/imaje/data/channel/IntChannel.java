package com.lhkbob.imaje.data.channel;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class IntChannel extends AbstractChannel implements IntSource, DataView<IntSource> {
  private final IntSource source;

  public IntChannel(IntSource source, long offset, long stride, long numPixels) {
    super(offset, stride, numPixels);
    this.source = source;
  }

  @Override
  public int get(long index) {
    return source.get(getSourceIndex(index));
  }

  @Override
  public IntSource getSource() {
    return source;
  }

  @Override
  public void set(long index, int value) {
    source.set(getSourceIndex(index), value);
  }
}
