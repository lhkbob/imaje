package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.icc.GenericColorValue;

/**
 *
 */
public class LuminanceToXYZ implements Transform {
  private final GenericColorValue whitepoint;

  public LuminanceToXYZ(GenericColorValue whitepoint) {
    if (whitepoint.getType() != GenericColorValue.ColorType.CIEXYZ && whitepoint.getType() != GenericColorValue.ColorType.PCSXYZ
        && whitepoint.getType() != GenericColorValue.ColorType.NORMALIZED_CIEXYZ) {
      throw new IllegalArgumentException("Whitepoint must be an XYZ color");
    }
    this.whitepoint = whitepoint;
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
    if (input.length != getInputChannels()) {
      throw new IllegalArgumentException(
          "Input vector must have " + getInputChannels() + " channels, but has " + input.length);
    }
    if (output.length != getOutputChannels()) {
      throw new IllegalArgumentException(
          "Output vector must have " + getOutputChannels() + " channels, but has " + output.length);
    }

    output[0] = whitepoint.getChannel(0) * input[0];
    output[1] = whitepoint.getChannel(1) * input[0];
    output[2] = whitepoint.getChannel(2) * input[0];
  }

  @Override
  public int hashCode() {
    return whitepoint.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof LuminanceToXYZ))
      return false;
    return ((LuminanceToXYZ) o).whitepoint.equals(whitepoint);
  }

  @Override
  public String toString() {
    return String.format("Luminance -> XYZ Transform (whitepoint: %s)", whitepoint);
  }
}
