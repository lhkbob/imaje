package com.lhkbob.imaje.data.channel;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class LongChannel extends AbstractChannel implements LongSource, DataView<LongSource> {
  private final LongSource source;

  public LongChannel(LongSource source, long offset, long stride, long numPixels) {
    super(offset, stride, numPixels);
    this.source = source;
  }

  @Override
  public long get(long index) {
    return source.get(getSourceIndex(index));
  }

  @Override
  public LongSource getSource() {
    return source;
  }

  @Override
  public void set(long index, long value) {
    source.set(getSourceIndex(index), value);
  }
}
