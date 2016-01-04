package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public class NormalizeXYZTransform implements ColorTransform {
  private final double whitepointLuminance;

  public NormalizeXYZTransform(double whitepointLuminance) {
    this.whitepointLuminance = whitepointLuminance;
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public NormalizeXYZTransform inverted() {
    return new NormalizeXYZTransform(1.0 / whitepointLuminance);
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

    for (int i = 0; i < 3; i++) {
      output[i] = input[i] / whitepointLuminance;
    }
  }
}
