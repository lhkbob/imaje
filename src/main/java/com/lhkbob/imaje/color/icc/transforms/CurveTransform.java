package com.lhkbob.imaje.color.icc.transforms;

import com.lhkbob.imaje.color.icc.curves.Curve;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class CurveTransform implements ColorTransform {
  private final List<Curve> curves;

  public CurveTransform(List<? extends Curve> curves) {
    this.curves = new ArrayList<>(curves);
  }

  @Override
  public int getInputChannels() {
    return curves.size();
  }

  @Override
  public int getOutputChannels() {
    return curves.size();
  }

  @Override
  public ColorTransform inverted() {
    List<Curve> inverted = curves.stream().map(Curve::inverted).collect(Collectors.toList());
    return new CurveTransform(inverted);
  }

  @Override
  public void transform(double[] input, double[] output) {
    if (input.length != getInputChannels()) {
      throw new IllegalArgumentException(
          "Input vector must have " + getInputChannels() + " channels, but has " + input.length);
    }
    if (output.length != getOutputChannels()) {
      throw new IllegalArgumentException(
          "Output vector must have " + getOutputChannels() + " channels, but has " + output.length);
    }

    for (int i = 0; i < input.length; i++) {
      output[i] = curves.get(i).evaluate(input[i]);
    }
  }
}
