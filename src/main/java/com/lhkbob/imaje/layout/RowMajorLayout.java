package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.IndexIterator;
import com.lhkbob.imaje.util.IndexSpliterator;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class RowMajorLayout implements PixelLayout {
  private final int width;
  private final int height;

  public RowMajorLayout(int pixelsPerRow, int numRows) {
    width = pixelsPerRow;
    height = numRows;
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
    return y * width + x;
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return new ImageCoordinate.Iterator(new IndexIterator(width * height), this::updateCoordinate);
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return new ImageCoordinate.Spliterator(
        new IndexSpliterator(width * height, width), this::updateCoordinate);
  }

  private void updateCoordinate(ImageCoordinate toUpdate, long index) {
    int y = (int) (index / width);
    int x = (int) (index - y * width);
    toUpdate.setX(x);
    toUpdate.setY(y);
  }
}
