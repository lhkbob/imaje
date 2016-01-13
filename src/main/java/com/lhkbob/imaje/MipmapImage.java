package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

import java.util.List;

/**
 *
 */
public interface MipmapImage<T extends Color> extends Image<T> {
  RasterImage<T> getLevel(int level);

  List<RasterImage<T>> getLevelImages();

  Pixel<T> getPixel(int x, int y, int level);
}
