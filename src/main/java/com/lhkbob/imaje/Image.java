package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.DefaultImageBuilder;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public interface Image<T extends Color> extends Iterable<Pixel<T>> {
  static <T extends Color> ImageBuilder.OfRaster<T> newRaster(Class<T> colorType) {
    return newRaster(Color.newInstance(colorType));
  }

  static <T extends Color> ImageBuilder.OfRaster<T> newRaster(T defaultColor) {
    return new DefaultImageBuilder.OfRaster<>(defaultColor);
  }

  static <T extends Color> ImageBuilder.OfMipmap<T> newMipmap(Class<T> colorType) {
    return newMipmap(Color.newInstance(colorType));
  }

  static <T extends Color> ImageBuilder.OfMipmap<T> newMipmap(T defaultColor) {
    return new DefaultImageBuilder.OfMipmap<>(defaultColor);
  }

  static <T extends Color> ImageBuilder.OfRasterArray<T> newRasterArray(Class<T> colorType) {
    return newRasterArray(Color.newInstance(colorType));
  }

  static <T extends Color> ImageBuilder.OfRasterArray<T> newRasterArray(T defaultColor) {
    return new DefaultImageBuilder.OfRasterArray<>(defaultColor);
  }

  static <T extends Color> ImageBuilder.OfMipmapArray<T> newMipmapArray(Class<T> colorType) {
    return newMipmapArray(Color.newInstance(colorType));
  }

  static <T extends Color> ImageBuilder.OfMipmapArray<T> newMipmapArray(T defaultColor) {
    return new DefaultImageBuilder.OfMipmapArray<>(defaultColor);
  }

  int getLayerCount();

  int getMipmapCount();

  default boolean isMipmapped() {
    return getMipmapCount() > 0;
  }

  default boolean isLayered() {
    return getLayerCount() > 0;
  }

  Pixel<T> getPixel(int x, int y, int mipmapLevel, int layer);

  Class<T> getColorType();

  boolean hasAlphaChannel();

  int getWidth();

  int getHeight();

  @Override
  Iterator<Pixel<T>> iterator();

  @Override
  Spliterator<Pixel<T>> spliterator();
}
