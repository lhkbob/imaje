package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface IndexedSampler2D<T extends Color> extends Sampler<T> {
  default double sample(double u, double v, int index, T result) {
    return sample(u, v, index, 0.0, result);
  }

  double sample(double u, double v, int index, double lod, T result);
}
