package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class XYZToHunterLab implements Transform {
  private final double ka;
  private final double kb;
  private final XYZ whitepoint;

  public XYZToHunterLab(XYZ whitepoint) {
    this(whitepoint, false);
  }

  XYZToHunterLab(XYZ whitepoint, boolean ownWhite) {
    this.whitepoint = (ownWhite ? whitepoint : whitepoint.clone());
    ka = calculateKA(whitepoint);
    kb = calculateKB(whitepoint);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToHunterLab)) {
      return false;
    }
    return ((XYZToHunterLab) o).whitepoint.equals(whitepoint);
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public XYZToHunterLab getLocallySafeInstance() {
    return this;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    return whitepoint.hashCode();
  }

  @Override
  public HunterLabToXYZ inverted() {
    return new HunterLabToXYZ(whitepoint, true);
  }

  @Override
  public String toString() {
    return String.format("XYZ -> Hunter Lab (whitepoint: %s)", whitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double xp = input[0] / whitepoint.x();
    double yp = input[1] / whitepoint.y();
    double rootYP = Math.sqrt(yp);
    double zp = input[2] / whitepoint.z();

    output[0] = 100.0 * rootYP;
    output[1] = ka * (xp - yp) / rootYP;
    output[2] = kb * (yp - zp) / rootYP;
  }

  static double calculateKA(XYZ whitepoint) {
    return 175.0 / 198.04 * (whitepoint.x() + whitepoint.y());
  }

  static double calculateKB(XYZ whitepoint) {
    return 70.0 / 218.11 * (whitepoint.y() + whitepoint.z());
  }
}
