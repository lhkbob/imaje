package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class YyxToXYZ implements Transform {
  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public XYZToYyx inverted() {
    return new XYZToYyx();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    // X from Y, y, and x
    output[0] = input[0] * input[1] / input[2];
    // Y from Y
    output[1] = input[0];
    // Z from Y, y, and x
    output[2] = input[0] * (1.0 - input[1] - input[2]) / input[2];
  }

  @Override
  public YyxToXYZ getLocallySafeInstance() {
    // This is purely functional so the instance can be used by any thread
    return this;
  }

  @Override
  public int hashCode() {
    return YyxToXYZ.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof YyxToXYZ;
  }

  @Override
  public String toString() {
    return "Yyx -> XYZ Transform";
  }
}
