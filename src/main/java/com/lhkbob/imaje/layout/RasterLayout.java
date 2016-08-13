package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.IndexIterator;
import com.lhkbob.imaje.util.IndexSpliterator;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class RasterLayout implements DataLayout {
  private final int imageWidth; // width in pixels of the image
  private final int imageHeight; // height in pixels of the image

  private final int channelCount;

  public RasterLayout(int imageWidth, int imageHeight, int channelCount) {
    Arguments.isPositive("imageWidth", imageWidth);
    Arguments.isPositive("imageHeight", imageHeight);
    Arguments.isPositive("channelCount", channelCount);

    this.channelCount = channelCount;

    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  @Override
  public int getHeight() {
    return imageHeight;
  }

  private void checkImageBounds(int x, int y) {
    Arguments.inRangeExcludeMax("x", 0, imageWidth, x);
    Arguments.inRangeExcludeMax("y", 0, imageHeight, y);
  }

  @Override
  public void getChannelIndices(int x, int y, long[] channelIndices) {
    Arguments.equals("channelIndices.length", channelCount, channelIndices.length);
    checkImageBounds(x, y);

    long base = channelCount * (y * imageWidth + x);
    for (int i = 0; i < channelIndices.length; i++) {
      channelIndices[i] = base + i;
    }
  }

  @Override
  public long getChannelIndex(int x, int y, int channel) {
    Arguments.inRangeExcludeMax("channel", 0, channelCount, channel);
    checkImageBounds(x, y);

    return channelCount * (y * imageWidth + x) + channel;
  }

  @Override
  public int getChannelCount() {
    return channelCount;
  }

  @Override
  public boolean isGPUCompatible() {
    return true;
  }

  @Override
  public int getWidth() {
    return imageWidth;
  }

  @Override
  public boolean isDataBottomToTop() {
    return true;
  }

  @Override
  public boolean isDataLeftToRight() {
    return true;
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return new ImageCoordinate.FastIterator(
        new IndexIterator(imageWidth * imageHeight), this::updateCoordinate);
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return new ImageCoordinate.FastSpliterator(
        new IndexSpliterator(imageWidth * imageHeight, imageWidth), this::updateCoordinate);
  }

  private void updateCoordinate(ImageCoordinate coord, long index) {
    int y = (int) (index / imageWidth);
    int x = (int) (index - y * imageWidth);
    coord.setX(x);
    coord.setY(y);
  }
}
