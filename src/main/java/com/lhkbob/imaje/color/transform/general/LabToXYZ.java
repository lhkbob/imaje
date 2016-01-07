package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;

/**
 *
 */
public class LabToXYZ implements Transform {
  static final double LINEAR_THRESHOLD = 6.0 / 29.0;
  static final double LINEAR_SLOPE = 3.0 * LINEAR_THRESHOLD * LINEAR_THRESHOLD;
  static final double LINEAR_OFFSET = 4.0 / 29.0; // ~0.138

  static final double L_SCALE = 1.0 / 116.0;
  static final double A_SCALE = 1.0 / 500.0;
  static final double B_SCALE = 1.0 / 200.0;

  private final XYZ referenceWhitepoint;

  public LabToXYZ(XYZ referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
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
  public XYZToLab inverted() {
    return new XYZToLab(referenceWhitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double lp = L_SCALE * (input[0] + 16.0);
    // X from L and a
    output[0] = referenceWhitepoint.x() * inverseF(lp + A_SCALE * input[1]);
    // Y from L
    output[1] = referenceWhitepoint.y() * inverseF(lp);
    // Z from L and b
    output[2] = referenceWhitepoint.z() * inverseF(lp - B_SCALE * input[2]);
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
    if (o == this) {
      return true;
    }
    if (!(o instanceof LabToXYZ)) {
      return false;
    }
    return ((LabToXYZ) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("L*a*b* -> XYZ Transform (whitepoint: %s)", referenceWhitepoint);
  }
}
