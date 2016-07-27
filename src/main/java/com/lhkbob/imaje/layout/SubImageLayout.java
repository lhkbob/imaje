package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.IndexIterator;
import com.lhkbob.imaje.util.IndexSpliterator;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class SubImageLayout implements PixelLayout {
  private final PixelLayout original;

  private final int offsetX;
  private final int offsetY;
  private final int width;
  private final int height;

  public SubImageLayout(PixelLayout original, int x, int y, int w, int h) {
    Arguments.notNull("original", original);
    Arguments.checkArrayRange("width", original.getWidth(), x, w);
    Arguments.checkArrayRange("height", original.getHeight(), y, h);

    this.original = original;
    offsetX = x;
    offsetY = y;
    width = w;
    height = h;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public void getChannelIndices(int x, int y, long[] channelIndices) {
    original.getChannelIndices(offsetX + x, offsetY + y, channelIndices);
  }

  @Override
  public long getChannelIndex(int x, int y, int channel) {
    return original.getChannelIndex(offsetX + x, offsetY + y, channel);
  }

  @Override
  public int getChannelCount() {
    return original.getChannelCount();
  }

  @Override
  public long getRequiredDataElements() {
    return original.getRequiredDataElements();
  }

  @Override
  public boolean isGPUCompatible() {
    return false;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return new ImageCoordinate.FastIterator(
        new IndexIterator(width * height), this::updateCoordinate);
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return new ImageCoordinate.FastSpliterator(
        new IndexSpliterator(width * height, width), this::updateCoordinate);
  }

  private void updateCoordinate(ImageCoordinate coord, long index) {
    int y = (int) (index / width);
    int x = (int) (index - y * width);
    coord.setX(x);
    coord.setY(y);
  }
}
