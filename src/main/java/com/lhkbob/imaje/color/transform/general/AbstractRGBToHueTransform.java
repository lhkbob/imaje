package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public abstract class AbstractRGBToHueTransform implements Transform {
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
    Transform.validateDimensions(this, input, output);

    double hue, min, max;
    if (input[0] >= input[1] && input[0] >= input[2]) {
      // Red is the largest component
      max = input[0];
      min = Math.min(input[1], input[2]);
      hue = ((input[1] - input[2]) / (max - min)) % 6.0;
    } else if (input[1] >= input[2]) {
      // Green is the largest component
      max = input[1];
      min = Math.min(input[0], input[2]);
      hue = (input[2] - input[0]) / (max - min) + 2.0;
    } else {
      // Blue is the largest component
      max = input[2];
      min = Math.min(input[0], input[1]);
      hue = (input[0] - input[1]) / (max - min) + 4.0;
    }

    output[0] = hue;
    output[1] = min;
    output[2] = max;
  }
}
