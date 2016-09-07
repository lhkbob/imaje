package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Sampler3D<T extends Color> extends Sampler<T> {
  default double sample(double u, double v, double w, T result) {
    return sample(u, v, w, 0.0, result);
  }

  double sample(double u, double v, double w, double lod, T result);
}
