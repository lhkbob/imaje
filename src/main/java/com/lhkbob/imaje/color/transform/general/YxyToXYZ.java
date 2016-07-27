package com.lhkbob.imaje.color.transform.general;

/**
 */
public class YxyToXYZ implements Transform {
  @Override
  public boolean equals(Object o) {
    return o instanceof YxyToXYZ;
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public YxyToXYZ getLocallySafeInstance() {
    // This is purely functional so the instance can be used by any thread
    return this;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    return YxyToXYZ.class.hashCode();
  }

  @Override
  public XYZToYxy inverted() {
    return new XYZToYxy();
  }

  @Override
  public String toString() {
    return "Yxy -> XYZ Transform";
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    // X from Y, x, and y
    output[0] = input[0] * input[1] / input[2];
    // Y from Y
    output[1] = input[0];
    // Z from Y, y, and x
    output[2] = input[0] * (1.0 - input[1] - input[2]) / input[2];
  }
}
