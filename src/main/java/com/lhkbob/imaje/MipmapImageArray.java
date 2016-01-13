package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

import java.util.List;

/**
 * FIXME I don't think this is adequate for describing a 3D image
 */
public interface MipmapImageArray<T extends Color> extends Image<T> {
  MipmapImage<T> getLayer(int index);

  List<MipmapImage<T>> getLayerImages();

  ImageArray<T> getLevel(int level);

  List<ImageArray<T>> getLevelImages();

  RasterImage<T> getRaster(int level, int index);
}
