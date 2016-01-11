package com.lhkbob.imaje.data.channel;

/**
 *
 */
public abstract class AbstractChannel {
  private final long offset;
  private final long stride;
  private final long numPixels;

  public AbstractChannel(long offset, long stride, long numPixels) {
    this.offset = offset;
    this.stride = stride;
    this.numPixels = numPixels;
  }

  public long getLength() {
    return numPixels;
  }

  public long getOffset() {
    return offset;
  }

  public long getStride() {
    return stride;
  }

  protected long getSourceIndex(long pixelIndex) {
    return offset + pixelIndex * stride;
  }
}
