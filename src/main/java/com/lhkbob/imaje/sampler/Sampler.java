package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Sampler<T extends Color> {
  Image<T> getImage();
}
