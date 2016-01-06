package com.lhkbob.imaje.color.icc.transforms;

import com.lhkbob.imaje.color.icc.GenericColorValue;

/**
 *
 */
public class LabToXYZTransform implements ColorTransform {
  static final double LINEAR_THRESHOLD = 6.0 / 29.0;
  static final double LINEAR_SLOPE = 3.0 * LINEAR_THRESHOLD * LINEAR_THRESHOLD;
  static final double LINEAR_OFFSET = 4.0 / 29.0; // ~0.138

  static final double L_SCALE = 1.0 / 116.0;
  static final double A_SCALE = 1.0 / 500.0;
  static final double B_SCALE = 1.0 / 200.0;

  private final GenericColorValue referenceWhitepoint;

  public LabToXYZTransform(GenericColorValue referenceWhitepoint) {
    if (referenceWhitepoint.getType() != GenericColorValue.ColorType.CIEXYZ &&
        referenceWhitepoint.getType() != GenericColorValue.ColorType.NORMALIZED_CIEXYZ
        && referenceWhitepoint.getType() != GenericColorValue.ColorType.PCSXYZ) {
      throw new IllegalArgumentException("Reference white point must be specified as XYZ");
    }
    this.referenceWhitepoint = referenceWhitepoint;
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public XYZToLabTransform inverted() {
    return new XYZToLabTransform(referenceWhitepoint);
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

    double lp = L_SCALE * (input[0] + 16.0);
    // X from L and a
    output[0] = referenceWhitepoint.getChannel(0) * inverseF(lp + A_SCALE * input[1]);
    // Y from L
    output[1] = referenceWhitepoint.getChannel(1) * inverseF(lp);
    // Z from L and b
    output[2] = referenceWhitepoint.getChannel(2) * inverseF(lp - B_SCALE * input[2]);
  }

  static double inverseF(double t) {
    if (t > LINEAR_THRESHOLD) {
      return t * t * t;
    } else {
      return LINEAR_SLOPE * (t - LINEAR_OFFSET);
    }
  }

  @Override
  public int hashCode() {
    return referenceWhitepoint.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof LabToXYZTransform))
      return false;
    return ((LabToXYZTransform) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("L*a*b* -> XYZ Transform (whitepoint: %s)", referenceWhitepoint);
  }
}
