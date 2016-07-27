package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class XYZToCIELab implements Transform {
  private final XYZ referenceWhitepoint;
  public XYZToCIELab(XYZ referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToCIELab)) {
      return false;
    }
    return ((XYZToCIELab) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public XYZToCIELab getLocallySafeInstance() {
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
  public CIELabToXYZ inverted() {
    return new CIELabToXYZ(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("XYZ -> CIELAB Transform (whitepoint: %s)", referenceWhitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double fx = f(input[0] / referenceWhitepoint.x());
    double fy = f(input[1] / referenceWhitepoint.y());
    double fz = f(input[2] / referenceWhitepoint.z());

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
  static final double LINEAR_OFFSET = 4.0 / 29.0; // ~0.138
  static final double LINEAR_SLOPE = Math.pow(29.0 / 6.0, 2.0) / 3.0; // ~7.787
  static final double LINEAR_THRESHOLD = Math.pow(6.0 / 29.0, 3.0); // ~0.009
}
