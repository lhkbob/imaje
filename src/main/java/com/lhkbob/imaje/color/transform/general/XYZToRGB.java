package com.lhkbob.imaje.color.transform.general;


import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.transform.curves.Curve;

import org.ejml.alg.fixed.FixedOps3;
import org.ejml.data.FixedMatrix3_64F;
import org.ejml.data.FixedMatrix3x3_64F;

/**
 *
 */
public class XYZToRGB implements Transform {
  private final Curve invGammaCurve;
  private final FixedMatrix3_64F workIn;
  private final FixedMatrix3_64F workOut;
  private final FixedMatrix3x3_64F xyzToLinearRGB;

  public XYZToRGB(FixedMatrix3x3_64F xyzToLinearRGB, Curve invGammaCurve) {
    this(xyzToLinearRGB, invGammaCurve, false);
  }

  XYZToRGB(FixedMatrix3x3_64F xyzToLinearRGB, Curve invGammaCurve, boolean ownMatrix) {
    this.xyzToLinearRGB = (ownMatrix ? xyzToLinearRGB : xyzToLinearRGB.copy());
    this.invGammaCurve = invGammaCurve;
    workIn = new FixedMatrix3_64F();
    workOut = new FixedMatrix3_64F();
  }

  public static FixedMatrix3x3_64F calculateXYZToLinearRGB(
      double xr, double yr, double xg, double yg, double xb, double yb, XYZ whitepoint) {
    FixedMatrix3x3_64F rgbToXYZ = RGBToXYZ
        .calculateLinearRGBToXYZ(xr, yr, xg, yg, xb, yb, whitepoint);
    FixedMatrix3x3_64F inv = new FixedMatrix3x3_64F();
    FixedOps3.invert(rgbToXYZ, inv);
    return inv;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToRGB)) {
      return false;
    }
    XYZToRGB t = (XYZToRGB) o;
    return (t.invGammaCurve == null ? invGammaCurve == null : t.invGammaCurve.equals(invGammaCurve))
        && Double.compare(t.xyzToLinearRGB.a11, xyzToLinearRGB.a11) == 0
        && Double.compare(t.xyzToLinearRGB.a12, xyzToLinearRGB.a12) == 0
        && Double.compare(t.xyzToLinearRGB.a13, xyzToLinearRGB.a13) == 0
        && Double.compare(t.xyzToLinearRGB.a21, xyzToLinearRGB.a21) == 0
        && Double.compare(t.xyzToLinearRGB.a22, xyzToLinearRGB.a22) == 0
        && Double.compare(t.xyzToLinearRGB.a23, xyzToLinearRGB.a23) == 0
        && Double.compare(t.xyzToLinearRGB.a31, xyzToLinearRGB.a31) == 0
        && Double.compare(t.xyzToLinearRGB.a32, xyzToLinearRGB.a32) == 0
        && Double.compare(t.xyzToLinearRGB.a33, xyzToLinearRGB.a33) == 0;
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public XYZToRGB getLocallySafeInstance() {
    return new XYZToRGB(xyzToLinearRGB, invGammaCurve, true);
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(xyzToLinearRGB.a11);
    result = 31 * result + Double.hashCode(xyzToLinearRGB.a12);
    result = 31 * result + Double.hashCode(xyzToLinearRGB.a13);
    result = 31 * result + Double.hashCode(xyzToLinearRGB.a21);
    result = 31 * result + Double.hashCode(xyzToLinearRGB.a22);
    result = 31 * result + Double.hashCode(xyzToLinearRGB.a23);
    result = 31 * result + Double.hashCode(xyzToLinearRGB.a31);
    result = 31 * result + Double.hashCode(xyzToLinearRGB.a32);
    result = 31 * result + Double.hashCode(xyzToLinearRGB.a33);
    result = 31 * result + (invGammaCurve != null ? invGammaCurve.hashCode() : 0);
    return result;
  }

  @Override
  public RGBToXYZ inverted() {
    Curve invGammaCurve = (this.invGammaCurve == null ? null : this.invGammaCurve.inverted());
    FixedMatrix3x3_64F linearRGBToXYZ = new FixedMatrix3x3_64F();
    FixedOps3.invert(xyzToLinearRGB, linearRGBToXYZ);
    return new RGBToXYZ(linearRGBToXYZ, invGammaCurve, true);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("XYZ -> RGB:\n");

    sb.append("  3x3 transform: [");
    for (int i = 0; i < 3; i++) {
      if (i > 0) {
        sb.append(",\n                 "); // padding for alignment
      }

      for (int j = 0; j < 3; j++) {
        if (j > 0) {
          sb.append(", ");
        }
        sb.append(String.format("%.3f", xyzToLinearRGB.get(i, j)));
      }
    }
    sb.append(']');

    if (invGammaCurve != null) {
      sb.append("\n  gamma correct: ").append(invGammaCurve);
    }
    return sb.toString();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    // Transform from XYZ to linear RGB
    workIn.a1 = input[0];
    workIn.a2 = input[1];
    workIn.a3 = input[2];
    FixedOps3.mult(xyzToLinearRGB, workIn, workOut);

    // Apply gamma correction
    if (invGammaCurve != null) {
      output[0] = invGammaCurve.evaluate(clampToCurveDomain(workOut.a1));
      output[1] = invGammaCurve.evaluate(clampToCurveDomain(workOut.a2));
      output[2] = invGammaCurve.evaluate(clampToCurveDomain(workOut.a3));
    } else {
      output[0] = workOut.a1;
      output[1] = workOut.a2;
      output[2] = workOut.a3;
    }
  }

  private double clampToCurveDomain(double c) {
    // Only called when invGammaCurve is not null
    return Math.max(invGammaCurve.getDomainMin(), Math.min(c, invGammaCurve.getDomainMax()));
  }
}
