package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Pixel;
import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface ImageStream<T extends Color> extends Iterable<Pixel<T>> {
  int getWidth();

  int getHeight();

  int getMipmapCount();

  int getLayerCount();

  default boolean isLayered() {
    return getLayerCount() > 1;
  }

  default boolean isMipmapped() {
    return getMipmapCount() > 1;
  }

  static <T extends Color> ImageStream<T> ofExisting(Image<T> image) {
    return new ExistingImageStream<>(image);
  }
}
