package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.icc.GenericColorValue;

/**
 *
 */
public class XYZToLab implements Transform {
  static final double LINEAR_THRESHOLD = Math.pow(6.0 / 29.0, 3.0); // ~0.009
  static final double LINEAR_SLOPE = Math.pow(29.0 / 6.0, 2.0) / 3.0; // ~7.787
  static final double LINEAR_OFFSET = 4.0 / 29.0; // ~0.138

  private final GenericColorValue referenceWhitepoint;

  public XYZToLab(GenericColorValue referenceWhitepoint) {
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
  public LabToXYZ inverted() {
    return new LabToXYZ(referenceWhitepoint);
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

    double fx = f(input[0] / referenceWhitepoint.getChannel(0));
    double fy = f(input[1] / referenceWhitepoint.getChannel(1));
    double fz = f(input[2] / referenceWhitepoint.getChannel(2));

    output[0] = 116.0 * fy - 16.0; // L*
    output[1] = 500 * (fx - fy); // a*
    output[2] = 200 * (fy - fz); // b*
  }

  static double f(double r) {
    if (r > LINEAR_THRESHOLD) {
      return Math.cbrt(r);
    } else {
      return LINEAR_SLOPE * r + LINEAR_OFFSET;
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
    if (!(o instanceof XYZToLab))
      return false;
    return ((XYZToLab) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("XYZ -> L*a*b* Transform (whitepoint: %s)", referenceWhitepoint);
  }
}
