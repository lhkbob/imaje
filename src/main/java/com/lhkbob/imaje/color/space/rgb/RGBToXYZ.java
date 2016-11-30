/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lhkbob.imaje.color.space.rgb;


import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import org.ejml.alg.fixed.FixedOps3;
import org.ejml.data.FixedMatrix3_64F;
import org.ejml.data.FixedMatrix3x3_64F;

/**
 *
 */
public class RGBToXYZ<I extends ColorSpace<RGB<I>, I>, O extends ColorSpace<XYZ<O>, O>> implements ColorTransform<I, RGB<I>, O, XYZ<O>> {
  private final I inputSpace;
  private final O outputSpace;

  private final Curve gammaCurve;
  private final FixedMatrix3x3_64F linearRGBToXYZ;

  private final XYZToRGB<O, I> inverse;

  private RGBToXYZ(
      I inputSpace, O outputSpace, FixedMatrix3x3_64F linearRGBToXYZ,
      @Arguments.Nullable Curve gammaCurve) {
    this.inputSpace = inputSpace;
    this.outputSpace = outputSpace;
    this.linearRGBToXYZ = linearRGBToXYZ;
    this.gammaCurve = gammaCurve;

    inverse = new XYZToRGB<>(this);
  }

  public Curve getGammaCurve() {
    return gammaCurve;
  }

  public FixedMatrix3x3_64F getLinearRGBToXYZ() {
    return linearRGBToXYZ.copy();
  }

  public static <I extends ColorSpace<RGB<I>, I>, O extends ColorSpace<XYZ<O>, O>> RGBToXYZ<I, O> newRGBToXYZ(
      I rgbSpace, XYZ<O> whitepoint, Yxy<O> redPrimary, Yxy<O> greenPrimary, Yxy<O> bluePrimary,
      @Arguments.Nullable Curve gammaCurve) {
    Arguments.notNull("rgbSpace", rgbSpace);

    FixedMatrix3x3_64F linearRGBToXYZ = calculateLinearRGBToXYZ(redPrimary.x(), redPrimary.y(),
        greenPrimary.x(), greenPrimary.y(), bluePrimary.x(), bluePrimary.y(), whitepoint);
    return new RGBToXYZ<>(rgbSpace, whitepoint.getColorSpace(), linearRGBToXYZ, gammaCurve);
  }

  private static FixedMatrix3x3_64F calculateLinearRGBToXYZ(
      double xr, double yr, double xg, double yg, double xb, double yb, XYZ<?> whitepoint) {
    Arguments.notNull("whitepoint", whitepoint);

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
    if (!t.inputSpace.equals(inputSpace) || !t.outputSpace.equals(outputSpace)) {
      return false;
    }
    if ((t.gammaCurve == null && gammaCurve != null) || (t.gammaCurve != null && !t.gammaCurve
        .equals(gammaCurve))) {
      return false;
    }

    return Double.compare(t.linearRGBToXYZ.a11, linearRGBToXYZ.a11) == 0
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
  public XYZToRGB<O, I> inverse() {
    return inverse;
  }

  @Override
  public I getInputSpace() {
    return inputSpace;
  }

  @Override
  public O getOutputSpace() {
    return outputSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    // Rely on escape analysis for performance
    FixedMatrix3_64F in = new FixedMatrix3_64F();
    FixedMatrix3_64F out = new FixedMatrix3_64F();

    // Apply linearization curve
    if (gammaCurve != null) {
      in.a1 = gammaCurve.evaluate(clampToCurveDomain(input[0]));
      in.a2 = gammaCurve.evaluate(clampToCurveDomain(input[1]));
      in.a3 = gammaCurve.evaluate(clampToCurveDomain(input[2]));
    } else {
      in.a1 = input[0];
      in.a2 = input[1];
      in.a3 = input[2];
    }

    // Transform by the matrix from linear RGB to XYZ
    FixedOps3.mult(linearRGBToXYZ, in, out);

    output[0] = out.a1;
    output[1] = out.a2;
    output[2] = out.a3;
    return true;
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
    result = 31 * result + inputSpace.hashCode();
    result = 31 * result + outputSpace.hashCode();
    return result;
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

  private double clampToCurveDomain(double c) {
    // Only called when curve is not null, so this is safe
    return Functions.clamp(c, gammaCurve.getDomainMin(), gammaCurve.getDomainMax());
  }
}
