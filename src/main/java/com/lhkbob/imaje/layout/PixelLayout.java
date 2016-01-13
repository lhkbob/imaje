package com.lhkbob.imaje.layout;

/**
 *
 */
public interface PixelLayout extends Iterable<ImageCoordinate> {
  int getWidth();

  int getHeight();

  long getIndex(int x, int y);
}
