package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.IndexIterator;
import com.lhkbob.imaje.util.IndexSpliterator;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class RasterLayout implements PixelLayout {
  private final int imageWidth; // width in pixels of the image
  private final int imageHeight; // height in pixels of the image

  private final long baseOffset;
  private final int channelCount;

  public RasterLayout(int imageWidth, int imageHeight, int channelCount) {
    this(imageWidth, imageHeight, channelCount, 0L);
  }

  public RasterLayout(
      int imageWidth, int imageHeight, int channelCount, long baseOffset) {
    if (imageWidth <= 0 || imageHeight <= 0) {
      throw new IllegalArgumentException(
          "Image dimensions must be at least 1: " + imageWidth + " x " + imageHeight);
    }
    if (channelCount <= 0) {
      throw new IllegalArgumentException("Channel count must be at least 1: " + channelCount);
    }
    if (baseOffset < 0) {
      throw new IndexOutOfBoundsException("Base offset cannot be negative: " + baseOffset);
    }

    this.baseOffset = baseOffset;
    this.channelCount = channelCount;

    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  @Override
  public int getHeight() {
    return imageHeight;
  }

  private void checkImageBounds(int x, int y) {
    if (x < 0 || x >= imageWidth || y < 0 || y >= imageHeight) {
      throw new IllegalArgumentException("(" + x + ", " + y + ") is outside image bounds");
    }
  }

  @Override
  public void getChannelIndices(int x, int y, long[] channelIndices) {
    if (channelIndices.length != channelCount) {
      throw new IllegalArgumentException("channelIndices length must equal " + channelCount);
    }
    checkImageBounds(x, y);

    long base = baseOffset + channelCount * (y * imageWidth + x);
    for (int i = 0; i < channelIndices.length; i++) {
      channelIndices[i] = base + i;
    }
  }

  @Override
  public long getChannelIndex(int x, int y, int channel) {
    if (channel < 0 || channel >= channelCount) {
      throw new IndexOutOfBoundsException("Invalid channel: " + channel);
    }
    checkImageBounds(x, y);

    return baseOffset + channelCount * (y * imageWidth + x) + channel;
  }

  @Override
  public int getChannelCount() {
    return channelCount;
  }

  @Override
  public long getRequiredDataElements() {
    return baseOffset + channelCount * imageWidth * imageHeight;
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
