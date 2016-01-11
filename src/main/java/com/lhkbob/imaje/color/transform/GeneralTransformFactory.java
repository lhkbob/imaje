package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.transform.general.Transform;

/**
 *
 */
public class GeneralTransformFactory<I extends Color, O extends Color> implements TransformFactory<I, O> {
  private final Class<I> inType;
  private final Class<O> outType;
  private final Transform inToOut;
  private final Transform outToIn;

  public GeneralTransformFactory(Class<I> in, Class<O> out, Transform inToOut, Transform outToIn) {
    inType = in;
    outType = out;
    this.inToOut = inToOut;
    this.outToIn = outToIn;
  }

  @Override
  public Class<I> getInputType() {
    return inType;
  }

  @Override
  public Class<O> getOutputType() {
    return outType;
  }

  @Override
  public ColorTransform<I, O> newTransform() {
    return new ColorTransformAdapter<>(inToOut.getLocallySafeInstance());
  }

  @Override
  public ColorTransform<O, I> newInverseTransform() {
    return new ColorTransformAdapter<>(outToIn.getLocallySafeInstance());
  }

  private static class ColorTransformAdapter<I extends Color, O extends Color> implements ColorTransform<I, O> {
    private final double[] inputValues;
    private final double[] outputValues;

    private final Transform transform;

    public ColorTransformAdapter(Transform transform) {
      this.transform = transform;
      inputValues = new double[transform.getInputChannels()];
      outputValues = new double[transform.getOutputChannels()];
    }

    @Override
    public boolean apply(I input, O output) {
      input.toArray(inputValues);
      transform.transform(inputValues, outputValues);
      output.fromArray(outputValues);

      return true;
    }
  }
}
