package com.lhkbob.imaje.data.channel;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class ShortChannel extends AbstractChannel implements ShortSource, DataView<ShortSource> {
  private final ShortSource source;

  public ShortChannel(ShortSource source, long offset, long stride, long numPixels) {
    super(offset, stride, numPixels);
    this.source = source;
  }

  @Override
  public short get(long index) {
    return source.get(getSourceIndex(index));
  }

  @Override
  public ShortSource getSource() {
    return source;
  }

  @Override
  public void set(long index, short value) {
    source.set(getSourceIndex(index), value);
  }
}
