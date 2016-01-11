package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public class InverseTransformFactory<I extends Color, O extends Color> implements TransformFactory<I, O> {
  private final TransformFactory<O, I> inverted;

  public InverseTransformFactory(TransformFactory<O, I> toInvert) {
    inverted = toInvert;
  }

  @Override
  public Class<I> getInputType() {
    return inverted.getOutputType();
  }

  @Override
  public Class<O> getOutputType() {
    return inverted.getInputType();
  }

  @Override
  public ColorTransform<I, O> newTransform() {
    return inverted.newInverseTransform();
  }

  @Override
  public ColorTransform<O, I> newInverseTransform() {
    return inverted.newTransform();
  }
}
