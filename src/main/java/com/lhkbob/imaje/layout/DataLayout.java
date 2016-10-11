package com.lhkbob.imaje.layout;

/**
 *
 */
public interface DataLayout extends Iterable<ImageCoordinate> {
  int getHeight();

  void getChannelIndices(int x, int y, long[] channelIndices);

  long getChannelIndex(int x, int y, int channel);

  int getChannelCount();

  default long getRequiredDataElements() {
    return getWidth() * getHeight() * getChannelCount();
  }

  boolean isGPUCompatible();

  int getWidth();

  static DataLayoutBuilder newBuilder() {
    return new DataLayoutBuilder();
  }

  boolean isDataBottomToTop();

  boolean isDataLeftToRight();
}
