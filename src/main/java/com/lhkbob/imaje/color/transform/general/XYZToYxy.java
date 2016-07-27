package com.lhkbob.imaje.color.transform.general;

/**
 */
public class XYZToYxy implements Transform {
  @Override
  public boolean equals(Object o) {
    return o instanceof XYZToYxy;
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public XYZToYxy getLocallySafeInstance() {
    // This is purely functional so the instance can be used by any thread
    return this;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    return XYZToYxy.class.hashCode();
  }

  @Override
  public YxyToXYZ inverted() {
    return new YxyToXYZ();
  }

  @Override
  public String toString() {
    return "XYZ -> Yxy Transform";
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double sum = input[0] + input[1] + input[2];
    // Y from Y
    output[0] = input[1];
    // x from X, Y, and Z
    output[1] = input[0] / sum;
    // y from X, Y, and Z
    output[2] = input[1] / sum;
  }
}
