package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface RasterImage<T extends Color> extends Image<T> {
  Pixel<T> getPixel(int x, int y);

  void get(int x, int y, T result);

  void set(int x, int y, T value);

  double getAlpha(int x, int y);

  void setAlpha(int x, int y, double alpha);
}
