package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;

/**
 *
 */
public class LuminanceToXYZ implements Transform {
  private final XYZ whitepoint;

  public LuminanceToXYZ(XYZ whitepoint) {
    this.whitepoint = whitepoint.clone();
  }

  @Override
  public int getInputChannels() {
    return 1;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public XYZToLuminance inverted() {
    return new XYZToLuminance(whitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    output[0] = whitepoint.x() * input[0];
    output[1] = whitepoint.y() * input[0];
    output[2] = whitepoint.z() * input[0];
  }

  @Override
  public int hashCode() {
    return whitepoint.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof LuminanceToXYZ)) {
      return false;
    }
    return ((LuminanceToXYZ) o).whitepoint.equals(whitepoint);
  }

  @Override
  public String toString() {
    return String.format("Luminance -> XYZ Transform (whitepoint: %s)", whitepoint);
  }
}
