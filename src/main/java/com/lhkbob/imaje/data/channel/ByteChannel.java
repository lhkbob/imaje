package com.lhkbob.imaje.data.channel;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataView;

/**
 *
 */
public class ByteChannel extends AbstractChannel implements ByteSource, DataView<ByteSource> {
  private final ByteSource source;

  public ByteChannel(ByteSource source, long offset, long stride, long numPixels) {
    super(offset, stride, numPixels);
    this.source = source;
  }

  @Override
  public byte get(long index) {
    return source.get(getSourceIndex(index));
  }

  @Override
  public ByteSource getSource() {
    return source;
  }

  @Override
  public void set(long index, byte value) {
    source.set(getSourceIndex(index), value);
  }
}
