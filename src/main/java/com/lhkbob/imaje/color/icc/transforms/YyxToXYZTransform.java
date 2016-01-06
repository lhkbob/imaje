package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public class YyxToXYZTransform implements ColorTransform {
  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public XYZToYyxTransform inverted() {
    return new XYZToYyxTransform();
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

    // X from Y, y, and x
    output[0] = input[0] * input[1] / input[2];
    // Y from Y
    output[1] = input[0];
    // Z from Y, y, and x
    output[2] = input[0] * (1.0 - input[1] - input[2]) / input[2];
  }

  @Override
  public int hashCode() {
    return YyxToXYZTransform.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof YyxToXYZTransform;
  }

  @Override
  public String toString() {
    return "Yyx -> XYZ Transform";
  }
}
