package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface RasterImage<T extends Color> extends Image<T> {
  void get(int x, int y, T result);

  double getAlpha(int x, int y);

  Pixel<T> getPixel(int x, int y);

  void set(int x, int y, T value);

  void setAlpha(int x, int y, double alpha);
}
