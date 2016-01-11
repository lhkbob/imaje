package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;

/**
 *
 */
public class XYZToLuminance implements Transform {
  private final XYZ whitepoint;

  public XYZToLuminance(XYZ whitepoint) {
    this.whitepoint = whitepoint.clone();
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public int getOutputChannels() {
    return 1;
  }

  @Override
  public LuminanceToXYZ inverted() {
    return new LuminanceToXYZ(whitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    output[0] = input[1] / whitepoint.y();
  }

  @Override
  public XYZToLuminance getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
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
    if (!(o instanceof XYZToLuminance)) {
      return false;
    }
    return ((XYZToLuminance) o).whitepoint.equals(whitepoint);
  }

  @Override
  public String toString() {
    return String.format("XYZ -> Luminance Transform (whitepoint: %s)", whitepoint);
  }
}
