package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface BoundaryStrategy<T extends Color> {
  int wrap(int texel, int dimension);

  boolean useBorder(int texel, int dimension);

  T getBorderColor();

  double getBorderAlpha();

  default boolean hasBorderColor() {
    return getBorderColor() != null;
  }
}
