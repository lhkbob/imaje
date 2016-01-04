package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public abstract class AbstractHueToRGBTransform implements ColorTransform {
  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public void transform(double[] input, double[] output) {
    // This assumes that input has been rewritten to hold hue, chroma, and m
    double chroma = input[1];
    double hp = input[0] / 60.0;
    double x = chroma * (1.0 - Math.abs(hp % 2.0 - 1.0));
    double m = input[2];

    if (hp < 1.0) {
      output[0] = chroma + m;
      output[1] = x + m;
      output[2] = m;
    } else if (hp < 2.0) {
      output[0] = x + m;
      output[1] = chroma + m;
      output[2] = m;
    } else if (hp < 3.0) {
      output[0] = m;
      output[1] = chroma + m;
      output[2] = x + m;
    } else if (hp < 4.0) {
      output[0] = m;
      output[1] = x + m;
      output[2] = chroma + m;
    } else if (hp < 5.0) {
      output[0] = x + m;
      output[1] = m;
      output[2] = chroma + m;
    } else {
      output[0] = chroma + m;
      output[1] = m;
      output[2] = x + m;
    }
  }

  protected void checkVectorDimensions(double[] input, double[] output) {
    if (input.length != getInputChannels()) {
      throw new IllegalArgumentException(
          "Input vector must have " + getInputChannels() + " channels, but has " + input.length);
    }
    if (output.length != getOutputChannels()) {
      throw new IllegalArgumentException(
          "Output vector must have " + getOutputChannels() + " channels, but has " + output.length);
    }
  }
}
