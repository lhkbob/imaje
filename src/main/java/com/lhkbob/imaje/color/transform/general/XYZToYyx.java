package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class XYZToYyx implements Transform {
  @Override
  public boolean equals(Object o) {
    return o instanceof XYZToYyx;
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public XYZToYyx getLocallySafeInstance() {
    // This is purely functional so the instance can be used by any thread
    return this;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    return XYZToYyx.class.hashCode();
  }

  @Override
  public YyxToXYZ inverted() {
    return new YyxToXYZ();
  }

  @Override
  public String toString() {
    return "XYZ -> Yyx Transform";
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double sum = input[0] + input[1] + input[2];
    // Y from Y
    output[0] = input[1];
    // y from X, Y, and Z
    output[1] = input[0] / sum;
    // x from X, Y, and Z
    output[2] = input[1] / sum;
  }
}
