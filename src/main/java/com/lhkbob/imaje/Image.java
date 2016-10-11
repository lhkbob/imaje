package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public interface Image<T extends Color> extends Iterable<Pixel<T>> {
  int getLayerCount();

  int getMipmapCount();

  default boolean isMipmapped() {
    return getMipmapCount() > 0;
  }

  default boolean isLayered() {
    return getLayerCount() > 0;
  }

  Pixel<T> getPixel(int layer, int mipmapLevel, int... coords);

  Class<T> getColorType();

  boolean hasAlphaChannel();

  int getDimensionality();

  int getDimension(int dim);

  default int[] getDimensions() {
    int[] d = new int[getDimensionality()];
    for (int i = 0; i < d.length; i++) {
      d[i] = getDimension(i);
    }
    return d;
  }

  default int getWidth() {
    return getDimension(0);
  }

  default int getHeight() {
    if (getDimensionality() > 1) {
      return getDimension(1);
    } else {
      return 1;
    }
  }

  default int getDepth() {
    if (getDimensionality() > 2) {
      return getDimension(2);
    } else {
      return 1;
    }
  }

  @Override
  Iterator<Pixel<T>> iterator();

  @Override
  Spliterator<Pixel<T>> spliterator();
}
