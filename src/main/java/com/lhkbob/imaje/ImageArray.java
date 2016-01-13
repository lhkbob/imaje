package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

import java.util.List;

/**
 *
 */
public interface ImageArray<T extends Color> extends Image<T> {
  RasterImage<T> getLayer(int index);

  List<RasterImage<T>> getLayerImages();

  Pixel<T> getPixel(int x, int y, int layer);
}
