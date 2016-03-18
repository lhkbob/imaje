package com.lhkbob.imaje.layout;

/**
 *
 */
public interface PixelLayout extends Iterable<ImageCoordinate> {
  int getHeight();

  void getChannelIndices(int x, int y, long[] channelIndices);

  long getChannelIndex(int x, int y, int channel);

  int getChannelCount();

  long getRequiredDataElements();

  boolean isGPUCompatible();

  int getWidth();
}
