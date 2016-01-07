package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.icc.GenericColorValue;

import static com.lhkbob.imaje.color.transform.general.XYZToLab.f;

/**
 *
 */
public class XYZToLuv implements Transform {
  private final GenericColorValue referenceWhitepoint;
  private final double uWhite, vWhite;

  public XYZToLuv(GenericColorValue referenceWhitepoint) {
    if (referenceWhitepoint.getType() != GenericColorValue.ColorType.CIEXYZ &&
        referenceWhitepoint.getType() != GenericColorValue.ColorType.NORMALIZED_CIEXYZ
        && referenceWhitepoint.getType() != GenericColorValue.ColorType.PCSXYZ) {
      throw new IllegalArgumentException("Reference white point must be specified as XYZ");
    }
    this.referenceWhitepoint = referenceWhitepoint;
    uWhite = uPrime(referenceWhitepoint.getChannel(0), referenceWhitepoint.getChannel(1), referenceWhitepoint.getChannel(2));
    vWhite = vPrime(referenceWhitepoint.getChannel(0), referenceWhitepoint.getChannel(1),
        referenceWhitepoint.getChannel(2));
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
  public LuvToXYZ inverted() {
    return new LuvToXYZ(referenceWhitepoint);
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

    double up = uPrime(input[0], input[1], input[2]);
    double vp = vPrime(input[0], input[1], input[2]);
    output[0] = 116.0 * f(input[1] / referenceWhitepoint.getChannel(1)) - 16.0; // L*
    output[1] = 13.0 * output[0] * (up - uWhite); // u*
    output[2] = 13.0 * output[0] * (vp - vWhite); // v*
  }

  static double uPrime(double x, double y, double z) {
    return 4.0 * x / (x + 15.0 * y + 3.0 * z);
  }

  static double vPrime(double x, double y, double z) {
    return 9.0 * y / (x + 15.0 * y + 3.0 * z);
  }

  @Override
  public int hashCode() {
    return referenceWhitepoint.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof XYZToLuv))
      return false;
    return ((XYZToLuv) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("XYZ -> L*u*v* Transform (whitepoint: %s)", referenceWhitepoint);
  }
}
