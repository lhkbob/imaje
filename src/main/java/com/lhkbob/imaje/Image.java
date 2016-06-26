package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public interface Image<T extends Color> extends Iterable<Pixel<T>> {

  static <T extends Color> ImageBuilder<T> builder(Class<T> colorType) {
    return new ImageBuilder<>(colorType);
  }

  static <T extends Color> ImageBuilder<T> builder(T defaultColor) {
    return new ImageBuilder<>(defaultColor);
  }

  Class<T> getColorType();

  boolean hasAlphaChannel();

  int getWidth();

  int getHeight();

  @Override
  Iterator<Pixel<T>> iterator();

  @Override
  Spliterator<Pixel<T>> spliterator();
}
