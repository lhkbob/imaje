package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.icc.GenericColorValue;

import static com.lhkbob.imaje.color.transform.general.LabToXYZ.L_SCALE;
import static com.lhkbob.imaje.color.transform.general.LabToXYZ.inverseF;

/**
 *
 */
public class LuvToXYZ implements Transform {
  private final GenericColorValue referenceWhitepoint;
  private final double uWhite, vWhite;

  public LuvToXYZ(GenericColorValue referenceWhitepoint) {
    if (referenceWhitepoint.getType() != GenericColorValue.ColorType.CIEXYZ &&
        referenceWhitepoint.getType() != GenericColorValue.ColorType.NORMALIZED_CIEXYZ
        && referenceWhitepoint.getType() != GenericColorValue.ColorType.PCSXYZ) {
      throw new IllegalArgumentException("Reference white point must be specified as XYZ");
    }
    this.referenceWhitepoint = referenceWhitepoint;
    uWhite = XYZToLuv.uPrime(referenceWhitepoint.getChannel(0), referenceWhitepoint.getChannel(1),
        referenceWhitepoint.getChannel(2));
    vWhite = XYZToLuv.vPrime(referenceWhitepoint.getChannel(0), referenceWhitepoint.getChannel(1),
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
  public XYZToLuv inverted() {
    return new XYZToLuv(referenceWhitepoint);
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

    double up = input[1] / (13.0 * input[0]) + uWhite;
    double vp = input[2] / (13.0 * input[0]) + vWhite;

    // Y from L*
    output[1] = referenceWhitepoint.getChannel(1) * inverseF(L_SCALE * (input[0] + 16.0));
    double denom = 9.0 * output[1] / vp;
    // X from Y, up, and denom
    output[0] = 0.25 * up * denom;
    // Z from Y, X, and denom
    output[2] = (denom - output[0] - 15.0 * output[1]) / 3.0;
  }

  @Override
  public int hashCode() {
    return referenceWhitepoint.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof LuvToXYZ))
      return false;
    return ((LuvToXYZ) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("L*u*v* -> XYZ Transform (whitepoint: %s)", referenceWhitepoint);
  }
}
