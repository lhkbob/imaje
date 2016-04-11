package com.lhkbob.imaje.layout;

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
    if (x < 0 || y < 0 || w < 0 || h < 0)
      throw new IllegalArgumentException("Offsets and dimensions cannot be negative");
    if (x + w > original.getWidth()) {
      throw new IllegalArgumentException("X offset and subimage width extend beyond width of original image");
    }
    if (y + h > original.getHeight()) {
      throw new IllegalArgumentException("Y offset and subimage height extend beyond height of original image");
    }

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
