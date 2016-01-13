package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;

/**
 *
 */
public class CIELabToXYZ implements Transform {
  private final XYZ referenceWhitepoint;
  public CIELabToXYZ(XYZ referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CIELabToXYZ)) {
      return false;
    }
    return ((CIELabToXYZ) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public CIELabToXYZ getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    return referenceWhitepoint.hashCode();
  }

  @Override
  public XYZToCIELab inverted() {
    return new XYZToCIELab(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("CIELAB -> XYZ Transform (whitepoint: %s)", referenceWhitepoint);
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
  static final double A_SCALE = 1.0 / 500.0;
  static final double B_SCALE = 1.0 / 200.0;
  static final double LINEAR_OFFSET = 4.0 / 29.0; // ~0.138
  static final double LINEAR_THRESHOLD = 6.0 / 29.0;
  static final double LINEAR_SLOPE = 3.0 * LINEAR_THRESHOLD * LINEAR_THRESHOLD;
  static final double L_SCALE = 1.0 / 116.0;
}
