package com.lhkbob.imaje.color.transform.general;


import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.transform.curves.Curve;

import org.ejml.alg.fixed.FixedOps3;
import org.ejml.data.FixedMatrix3_64F;
import org.ejml.data.FixedMatrix3x3_64F;

/**
 *
 */
public class RGBToXYZ implements Transform {
  private final Curve gammaCurve;
  private final FixedMatrix3x3_64F linearRGBToXYZ;
  private final FixedMatrix3_64F workIn;
  private final FixedMatrix3_64F workOut;

  public RGBToXYZ(FixedMatrix3x3_64F linearRGBToXYZ, Curve gammaCurve) {
    this(linearRGBToXYZ, gammaCurve, false);
  }

  RGBToXYZ(FixedMatrix3x3_64F linearRGBToXYZ, Curve gammaCurve, boolean ownMatrix) {
    this.linearRGBToXYZ = (ownMatrix ? linearRGBToXYZ : linearRGBToXYZ.copy());
    this.gammaCurve = gammaCurve;
    workIn = new FixedMatrix3_64F();
    workOut = new FixedMatrix3_64F();
  }

  public static FixedMatrix3x3_64F calculateLinearRGBToXYZ(
      double xr, double yr, double xg, double yg, double xb, double yb, XYZ whitepoint) {
    double zr = 1.0 - xr - yr;
    double zg = 1.0 - xg - yg;
    double zb = 1.0 - xb - yb;

    FixedMatrix3x3_64F system = new FixedMatrix3x3_64F(xr, xg, xb, yr, yg, yb, zr, zg, zb);
    FixedMatrix3x3_64F inv = new FixedMatrix3x3_64F();
    if (!FixedOps3.invert(system, inv)) {
      throw new IllegalArgumentException("Cannot form conversion matrix with given primaries");
    }

    FixedMatrix3_64F w = new FixedMatrix3_64F(whitepoint.x(), whitepoint.y(), whitepoint.z());
    FixedMatrix3_64F s = new FixedMatrix3_64F();
    FixedOps3.mult(inv, w, s);

    system.a11 *= s.a1;
    system.a21 *= s.a1;
    system.a31 *= s.a1;

    system.a12 *= s.a2;
    system.a22 *= s.a2;
    system.a32 *= s.a2;

    system.a13 *= s.a3;
    system.a23 *= s.a3;
    system.a33 *= s.a3;

    return system;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof RGBToXYZ)) {
      return false;
    }
    RGBToXYZ t = (RGBToXYZ) o;
    return (t.gammaCurve == null ? gammaCurve == null : t.gammaCurve.equals(gammaCurve))
        && Double.compare(t.linearRGBToXYZ.a11, linearRGBToXYZ.a11) == 0
        && Double.compare(t.linearRGBToXYZ.a12, linearRGBToXYZ.a12) == 0
        && Double.compare(t.linearRGBToXYZ.a13, linearRGBToXYZ.a13) == 0
        && Double.compare(t.linearRGBToXYZ.a21, linearRGBToXYZ.a21) == 0
        && Double.compare(t.linearRGBToXYZ.a22, linearRGBToXYZ.a22) == 0
        && Double.compare(t.linearRGBToXYZ.a23, linearRGBToXYZ.a23) == 0
        && Double.compare(t.linearRGBToXYZ.a31, linearRGBToXYZ.a31) == 0
        && Double.compare(t.linearRGBToXYZ.a32, linearRGBToXYZ.a32) == 0
        && Double.compare(t.linearRGBToXYZ.a33, linearRGBToXYZ.a33) == 0;
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public RGBToXYZ getLocallySafeInstance() {
    return new RGBToXYZ(linearRGBToXYZ, gammaCurve, true);
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(linearRGBToXYZ.a11);
    result = 31 * result + Double.hashCode(linearRGBToXYZ.a12);
    result = 31 * result + Double.hashCode(linearRGBToXYZ.a13);
    result = 31 * result + Double.hashCode(linearRGBToXYZ.a21);
    result = 31 * result + Double.hashCode(linearRGBToXYZ.a22);
    result = 31 * result + Double.hashCode(linearRGBToXYZ.a23);
    result = 31 * result + Double.hashCode(linearRGBToXYZ.a31);
    result = 31 * result + Double.hashCode(linearRGBToXYZ.a32);
    result = 31 * result + Double.hashCode(linearRGBToXYZ.a33);
    result = 31 * result + (gammaCurve != null ? gammaCurve.hashCode() : 0);
    return result;
  }

  @Override
  public XYZToRGB inverted() {
    Curve gammaCurve = (this.gammaCurve == null ? null : this.gammaCurve.inverted());
    FixedMatrix3x3_64F xyzToLinearRGB = new FixedMatrix3x3_64F();
    FixedOps3.invert(linearRGBToXYZ, xyzToLinearRGB);
    return new XYZToRGB(xyzToLinearRGB, gammaCurve, true);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("RGB -> XYZ:\n");
    if (gammaCurve != null) {
      sb.append("  linearization: ").append(gammaCurve).append('\n');
    }

    sb.append("  3x3 transform: [");
    for (int i = 0; i < 3; i++) {
      if (i > 0) {
        sb.append(",\n                 "); // padding for alignment
      }

      for (int j = 0; j < 3; j++) {
        if (j > 0) {
          sb.append(", ");
        }
        sb.append(String.format("%.3f", linearRGBToXYZ.get(i, j)));
      }
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    // Apply linearization curve
    if (gammaCurve != null) {
      workIn.a1 = gammaCurve.evaluate(clampToCurveDomain(input[0]));
      workIn.a2 = gammaCurve.evaluate(clampToCurveDomain(input[1]));
      workIn.a3 = gammaCurve.evaluate(clampToCurveDomain(input[2]));
    } else {
      workIn.a1 = input[0];
      workIn.a2 = input[1];
      workIn.a3 = input[2];
    }

    // Transform by the matrix from linear RGB to XYZ
    FixedOps3.mult(linearRGBToXYZ, workIn, workOut);

    output[0] = workOut.a1;
    output[1] = workOut.a2;
    output[2] = workOut.a3;
  }

  private double clampToCurveDomain(double c) {
    // Only called when curve is not null, so this is safe
    return Math.max(gammaCurve.getDomainMin(), Math.min(c, gammaCurve.getDomainMax()));
  }
}
