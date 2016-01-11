package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;

import static com.lhkbob.imaje.color.transform.general.XYZToHunterLab.calculateKA;
import static com.lhkbob.imaje.color.transform.general.XYZToHunterLab.calculateKB;

/**
 *
 */
public class HunterLabToXYZ implements Transform {
  private final XYZ whitepoint;
  private final double invKA;
  private final double invKB;

  public HunterLabToXYZ(XYZ whitepoint) {
    this(whitepoint, false);
  }

  HunterLabToXYZ(XYZ whitepoint, boolean ownWhite) {
    this.whitepoint = (ownWhite ? whitepoint : whitepoint.clone());
    invKA = 1.0 / calculateKA(whitepoint);
    invKB = 1.0 / calculateKB(whitepoint);
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
  public XYZToHunterLab inverted() {
    return new XYZToHunterLab(whitepoint, true);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double rootYP = input[0] / 100.0;
    double yp = rootYP * rootYP;
    double xp = input[1] * rootYP * invKA + yp;
    double zp = yp - input[2] * rootYP * invKB;

    output[0] = xp * whitepoint.x();
    output[1] = yp * whitepoint.y();
    output[2] = zp * whitepoint.z();
  }

  @Override
  public HunterLabToXYZ getLocallySafeInstance() {
    return this;
  }

  @Override
  public int hashCode() {
    return whitepoint.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof HunterLabToXYZ)) {
      return false;
    }
    return ((HunterLabToXYZ) o).whitepoint.equals(whitepoint);
  }

  @Override
  public String toString() {
    return String.format("Hunter Lab -> XYZ (whitepoint: %s)", whitepoint);
  }
}
