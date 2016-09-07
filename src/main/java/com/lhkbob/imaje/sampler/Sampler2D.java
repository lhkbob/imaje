package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Sampler2D<T extends Color> extends Sampler<T> {
  default double sample(double u, double v, T result) {
    return sample(u, v, 0.0, result);
  }

  // FIXME LOD from 0 to 1 is slightly inconsistent with how OpenGL does it, perhaps
  // it would be better to go from 0 to level_max as they seem to for consistency?
  // Should decide this after implementing LOD calculation functions based on u, v, w derivatives
  // and see if they can be converted/treated as if the final value is 0 to 1.
  double sample(double u, double v, double lod, T result);
}
