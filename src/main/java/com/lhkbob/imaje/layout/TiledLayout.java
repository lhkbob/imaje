package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.IndexIterator;
import com.lhkbob.imaje.util.IndexSpliterator;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class TiledLayout implements PixelLayout {
  // Number of pixels to advance to go to start of next tile along top row.
  // NOTE: this is 0 if the image dimensions are an integer number of tiles along y axis
  private final long topTileSkip;
  // Number of pixels to advance to go to start of next full tile
  private final long fullTileSkip;
  // Number of pixels to advance to go to start of the next row of tiles (not bottom).
  private final long imageRowSkip;

  // Number of pixels to advance to start of next pixel row when in a right-edge tile.
  // NOTE this is 0 if the image dimensions are an integer number of tiles along x axis
  private final int rightTileWidth;
  // Number of pixels per tile row.
  private final int tileWidth;
  // Number of pixels per tile height.
  private final int tileHeight;

  // Number of full tiles in a row.
  private final int fullTilesPerRow;
  // Number of full rows per image.
  private final int fullRowsPerImage;

  private final int width;
  private final int height;

  public TiledLayout(int pixelsPerRow, int numRows, int tileWidth, int tileHeight) {
    width = pixelsPerRow;
    height = numRows;

    // Easy measurements that are based on full rows or tiles, etc.
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;

    imageRowSkip = pixelsPerRow * tileHeight;
    fullTileSkip = tileWidth * tileHeight;

    // Hanging pixels along the right edge for partial tiles
    fullTilesPerRow = pixelsPerRow / tileWidth;
    rightTileWidth = pixelsPerRow - fullTilesPerRow * tileWidth;

    // Hanging pixels along the bottom edge for partial tiles
    fullRowsPerImage = numRows / tileHeight;
    topTileSkip = tileWidth * (numRows - fullRowsPerImage * tileHeight);
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public long getIndex(int x, int y) {
    long tileX = x / tileWidth;
    long tileY = y / tileHeight;

    long offset = tileY * imageRowSkip;
    offset += (tileY >= fullRowsPerImage ? topTileSkip : fullTileSkip) * tileX;

    long subTileX = x % tileWidth;
    long subTileY = y % tileHeight;
    offset += subTileY * (tileX >= fullTilesPerRow ? rightTileWidth : tileWidth);
    offset += subTileX;

    return offset;
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return new ImageCoordinate.Iterator(new IndexIterator(width * height), this::updateCoordinate);
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return new ImageCoordinate.Spliterator(
        new IndexSpliterator(width * height, tileWidth), this::updateCoordinate);
  }

  private void updateCoordinate(ImageCoordinate toUpdate, long index) {
    int tileY = (int) (index / imageRowSkip);
    long tileRowRemaining = index - tileY * imageRowSkip;

    long tileSize = tileY >= fullRowsPerImage ? topTileSkip : fullTileSkip;

    int tileX = (int) (tileRowRemaining / tileSize);
    long tileRemaining = tileRowRemaining - tileX * tileSize;

    long rowSize = tileX >= fullTilesPerRow ? rightTileWidth : tileWidth;
    int subY = (int) (tileRemaining / rowSize);
    int subX = (int) (tileRemaining - subY * rowSize);

    toUpdate.setX(tileX * tileWidth + subX);
    toUpdate.setY(tileY * tileHeight + subY);
  }
}
