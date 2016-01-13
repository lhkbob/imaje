package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;

/**
 *
 */
public interface Image<T extends Color> extends Iterable<Pixel<T>> {
  static int getMipmapCount(int maxDimension) {
    return (int) Math.floor(Math.log(maxDimension) / Math.log(2.0)) + 1;
  }

  static int getMipmapCount(int width, int height) {
    return getMipmapCount(Math.max(width, height));
  }

  static int getMipmapDimension(int topLevelDimension, int level) {
    return Math.max(topLevelDimension >> level, 1);
  }

  static <T extends Color> ImageBuilder<T> of(Class<T> colorType) {
    return new ImageBuilder<>(colorType);
  }

  Class<T> getColorType();

  int getHeight();

  int getLayerCount();

  Map<String, String> getMetadata();

  int getMipmapCount();

  Pixel<T> getPixel(int x, int y, int level, int layer);

  int getWidth();

  boolean hasAlphaChannel();

  @Override
  Iterator<Pixel<T>> iterator();

  @Override
  Spliterator<Pixel<T>> spliterator();
}
