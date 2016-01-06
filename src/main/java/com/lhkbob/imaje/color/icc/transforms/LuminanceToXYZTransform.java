package com.lhkbob.imaje.color.icc.transforms;

import com.lhkbob.imaje.color.icc.GenericColorValue;

/**
 *
 */
public class LuminanceToXYZTransform implements ColorTransform {
  private final GenericColorValue whitepoint;

  public LuminanceToXYZTransform(GenericColorValue whitepoint) {
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
  public XYZToLuminanceTransform inverted() {
    return new XYZToLuminanceTransform(whitepoint);
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
}
