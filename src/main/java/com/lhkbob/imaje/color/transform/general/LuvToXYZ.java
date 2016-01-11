package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;

import static com.lhkbob.imaje.color.transform.general.CIELabToXYZ.L_SCALE;
import static com.lhkbob.imaje.color.transform.general.CIELabToXYZ.inverseF;

/**
 *
 */
public class LuvToXYZ implements Transform {
  private final XYZ referenceWhitepoint;
  private final double uWhite, vWhite;

  public LuvToXYZ(XYZ referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
    uWhite = XYZToLuv.uPrime(referenceWhitepoint.x(), referenceWhitepoint.y(),
        referenceWhitepoint.z());
    vWhite = XYZToLuv.vPrime(referenceWhitepoint.x(), referenceWhitepoint.y(),
        referenceWhitepoint.z());
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
    Transform.validateDimensions(this, input, output);

    double up = input[1] / (13.0 * input[0]) + uWhite;
    double vp = input[2] / (13.0 * input[0]) + vWhite;

    // Y from L*
    output[1] = referenceWhitepoint.y() * inverseF(L_SCALE * (input[0] + 16.0));
    double denom = 9.0 * output[1] / vp;
    // X from Y, up, and denom
    output[0] = 0.25 * up * denom;
    // Z from Y, X, and denom
    output[2] = (denom - output[0] - 15.0 * output[1]) / 3.0;
  }

  @Override
  public LuvToXYZ getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
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
