package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Sampler2D<T extends Color> extends Sampler<T> {
  default double sample(double u, double v, T result) {
    return sample(u, v, 0.0, result);
  }

  double sample(double u, double v, double lod, T result);
}
