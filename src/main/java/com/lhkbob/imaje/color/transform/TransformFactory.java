package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface TransformFactory<I extends Color, O extends Color> {
  Class<I> getInputType();

  Class<O> getOutputType();

  ColorTransform<O, I> newInverseTransform();

  ColorTransform<I, O> newTransform();
}
