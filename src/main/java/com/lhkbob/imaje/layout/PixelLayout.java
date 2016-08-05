package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.PixelLayoutBuilder;

/**
 *
 */
public interface PixelLayout extends Iterable<ImageCoordinate> {
  int getHeight();

  void getChannelIndices(int x, int y, long[] channelIndices);

  long getChannelIndex(int x, int y, int channel);

  int getChannelCount();

  default long getRequiredDataElements() {
    return getWidth() * getHeight() * getChannelCount();
  }

  boolean isGPUCompatible();

  int getWidth();

  static PixelLayoutBuilder newBuilder() {
    return new PixelLayoutBuilder();
  }

  boolean isDataBottomToTop();

  boolean isDataLeftToRight();
}
