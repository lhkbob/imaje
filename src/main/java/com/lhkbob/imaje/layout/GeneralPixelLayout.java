package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.IndexIterator;
import com.lhkbob.imaje.util.IndexSpliterator;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class GeneralPixelLayout implements DataLayout {
  public enum InterleavingUnit {
    PIXEL, SCANLINE, TILE, IMAGE
  }

  private final int imageWidth; // width in pixels of the image
  private final int imageHeight; // height in pixels of the image

  private final int tileWidth; // full width in pixels of a tile
  private final int hangingTileWidth; // remainder if tileWidth does not evenly divide imageWidth
  private final int tileColumnCount; // total number of full tile columns (excluding any hanging tile)

  private final int tileHeight; // full height in pixels of a tile
  private final int hangingTileHeight; // remainder if tileHeight does not evenly divide imageHeight
  private final int tileRowCount; // total number of full tile rows (excluding any hanging tile)

  private final int channelCount;
  private final InterleavingUnit interleave;

  public GeneralPixelLayout(int imageWidth, int imageHeight, int channelCount) {
    this(imageWidth, imageHeight, imageWidth, imageHeight, channelCount);
  }

  public GeneralPixelLayout(
      int imageWidth, int imageHeight, int tileWidth, int tileHeight, int channelCount) {
    this(imageWidth, imageHeight, tileWidth, tileHeight, channelCount, InterleavingUnit.PIXEL);
  }

  public GeneralPixelLayout(
      int imageWidth, int imageHeight, int tileWidth, int tileHeight, int channelCount,
      InterleavingUnit interleave) {
    Arguments.isPositive("imageWidth", imageWidth);
    Arguments.isPositive("imageHeight", imageHeight);
    Arguments.isPositive("tileWidth", tileWidth);
    Arguments.isPositive("tileHeight", tileHeight);
    Arguments.isPositive("channelCount", channelCount);

    this.channelCount = channelCount;
    this.interleave = interleave;

    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;

    tileColumnCount = imageWidth / tileWidth;
    tileRowCount = imageHeight / tileHeight;
    hangingTileWidth = imageWidth - tileWidth * tileColumnCount;
    hangingTileHeight = imageHeight - tileHeight * tileRowCount;
  }

  public int getTileWidth() {
    return tileWidth;
  }

  public int getTileHeight() {
    return tileHeight;
  }

  public InterleavingUnit getInterleavingUnit() {
    return interleave;
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

    int tileX = x / tileWidth;
    int tileY = y / tileHeight;

    int withinTileX = x % tileWidth;
    int withinTileY = y % tileHeight;

    int actualTileWidth = tileX >= tileColumnCount ? hangingTileWidth : tileWidth;
    int actualTileHeight = tileY >= tileRowCount ? hangingTileHeight : tileHeight;

    // PIXEL
    int tileStride = channelCount;
    int withinTileStride = channelCount;
    int pixelStride = channelCount;
    int offset = 1;
    switch (interleave) {
    case SCANLINE:
      tileStride = channelCount;
      withinTileStride = channelCount;
      pixelStride = 1;
      offset = actualTileWidth;
      break;
    case TILE:
      tileStride = channelCount;
      withinTileStride = 1;
      pixelStride = 1;
      offset = actualTileWidth * actualTileHeight;
      break;
    case IMAGE:
      tileStride = 1;
      withinTileStride = 1;
      pixelStride = 1;
      offset = imageWidth * imageHeight;
      break;
    }

    long base = tileStride * (tileY * tileHeight * imageWidth
        + tileX * tileWidth * actualTileHeight) + withinTileStride * withinTileY * actualTileWidth
        + pixelStride * withinTileX;
    for (int i = 0; i < channelIndices.length; i++) {
      channelIndices[i] = base + i * offset;
    }
  }

  @Override
  public long getChannelIndex(int x, int y, int channel) {
    Arguments.inRangeExcludeMax("channel", 0, channelCount, channel);
    checkImageBounds(x, y);

    int tileX = x / tileWidth;
    int tileY = y / tileHeight;

    int withinTileX = x % tileWidth;
    int withinTileY = y % tileHeight;

    int actualTileWidth = tileX >= tileColumnCount ? hangingTileWidth : tileWidth;
    int actualTileHeight = tileY >= tileRowCount ? hangingTileHeight : tileHeight;

    // PIXEL
    int tileStride = channelCount;
    int withinTileStride = channelCount;
    int pixelStride = channelCount;
    int offset = 1;
    switch (interleave) {
    case SCANLINE:
      tileStride = channelCount;
      withinTileStride = channelCount;
      pixelStride = 1;
      offset = actualTileWidth;
      break;
    case TILE:
      tileStride = channelCount;
      withinTileStride = 1;
      pixelStride = 1;
      offset = actualTileWidth * actualTileHeight;
      break;
    case IMAGE:
      tileStride = 1;
      withinTileStride = 1;
      pixelStride = 1;
      offset = imageWidth * imageHeight;
      break;
    }

    return tileStride * (tileY * tileHeight * imageWidth
        + tileX * tileWidth * actualTileHeight) + withinTileStride * withinTileY * actualTileWidth
        + pixelStride * withinTileX + channel * offset;
  }

  @Override
  public int getChannelCount() {
    return channelCount;
  }

  @Override
  public boolean isGPUCompatible() {
    return interleave == InterleavingUnit.PIXEL && tileWidth == imageWidth
        && tileHeight == imageHeight;
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
        new IndexSpliterator(imageWidth * imageHeight, tileWidth), this::updateCoordinate);
  }

  private void updateCoordinate(ImageCoordinate coord, long index) {
    long tileRowSize = tileHeight * imageWidth;
    int tileY = (int) (index / tileRowSize);
    index = index - tileY * tileRowSize;
    long tileSize = (tileY >= tileRowCount ? hangingTileHeight : tileHeight) * tileWidth;
    int tileX = (int) (index / tileSize);
    index = index - tileX * tileSize;

    long actualTileWidth = (tileX >= tileColumnCount ? hangingTileWidth : tileWidth);
    int withinTileY = (int) (index / actualTileWidth);
    int withinTileX = (int) (index - withinTileY * actualTileWidth);

    coord.setX(tileX * tileWidth + withinTileX);
    coord.setY(tileY * tileHeight + withinTileY);
  }
}
