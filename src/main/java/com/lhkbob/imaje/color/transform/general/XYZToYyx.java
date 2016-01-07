package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class XYZToYyx implements Transform {
  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public YyxToXYZ inverted() {
    return new YyxToXYZ();
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

    double sum = input[0] + input[1] + input[2];
    // Y from Y
    output[0] = input[1];
    // y from X, Y, and Z
    output[1] = input[0] / sum;
    // x from X, Y, and Z
    output[2] = input[1] / sum;
  }

  @Override
  public int hashCode() {
    return XYZToYyx.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof XYZToYyx;
  }

  @Override
  public String toString() {
    return "XYZ -> Yyx Transform";
  }
}
