package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;

import static com.lhkbob.imaje.color.transform.general.XYZToLab.f;

/**
 *
 */
public class XYZToLuv implements Transform {
  private final XYZ referenceWhitepoint;
  private final double uWhite, vWhite;

  public XYZToLuv(XYZ referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint;
    uWhite = uPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    vWhite = vPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
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
    Transform.validateDimensions(this, input, output);

    double up = uPrime(input[0], input[1], input[2]);
    double vp = vPrime(input[0], input[1], input[2]);
    output[0] = 116.0 * f(input[1] / referenceWhitepoint.y()) - 16.0; // L*
    output[1] = 13.0 * output[0] * (up - uWhite); // u*
    output[2] = 13.0 * output[0] * (vp - vWhite); // v*
  }

  @Override
  public XYZToLuv getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
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
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToLuv)) {
      return false;
    }
    return ((XYZToLuv) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("XYZ -> L*u*v* Transform (whitepoint: %s)", referenceWhitepoint);
  }
}
