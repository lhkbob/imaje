package com.lhkbob.imaje.color.icc.transforms;

import com.lhkbob.imaje.color.icc.GenericColorValue;

/**
 *
 */
public class XYZToLuminanceTransform implements ColorTransform {
  private final GenericColorValue whitepoint;

  public XYZToLuminanceTransform(GenericColorValue whitepoint) {
    if (whitepoint.getType() != GenericColorValue.ColorType.CIEXYZ && whitepoint.getType() != GenericColorValue.ColorType.PCSXYZ
        && whitepoint.getType() != GenericColorValue.ColorType.NORMALIZED_CIEXYZ) {
      throw new IllegalArgumentException("Whitepoint must be an XYZ color");
    }
    this.whitepoint = whitepoint;
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
  public LuminanceToXYZTransform inverted() {
    return new LuminanceToXYZTransform(whitepoint);
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

    output[0] = input[1] / whitepoint.getChannel(1);
  }
}
