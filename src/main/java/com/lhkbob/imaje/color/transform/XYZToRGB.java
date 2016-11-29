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
package com.lhkbob.imaje.color.transform;


import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import org.ejml.alg.fixed.FixedOps3;
import org.ejml.data.FixedMatrix3_64F;
import org.ejml.data.FixedMatrix3x3_64F;

/**
 *
 */
public class XYZToRGB<I extends ColorSpace<XYZ<I>, I>, O extends ColorSpace<RGB<O>, O>> implements ColorTransform<I, XYZ<I>, O, RGB<O>> {
  private final Curve invGammaCurve;
  private final FixedMatrix3x3_64F xyzToLinearRGB;

  private final RGBToXYZ<O, I> inverse;

  XYZToRGB(RGBToXYZ<O, I> inverse) {
    this.inverse = inverse;
    FixedMatrix3x3_64F rgbToXYZ = inverse.getLinearRGBToXYZ();
    xyzToLinearRGB = new FixedMatrix3x3_64F();
    FixedOps3.invert(rgbToXYZ, xyzToLinearRGB);

    Curve gamma = inverse.getGammaCurve();
    if (gamma == null) {
      invGammaCurve = null;
    } else {
      invGammaCurve = gamma.inverted();
    }
  }

  public static <I extends ColorSpace<RGB<I>, I>, O extends ColorSpace<XYZ<O>, O>> XYZToRGB<O, I> newXYZToRGB(
      I rgbSpace, XYZ<O> whitepoint, Yxy<O> redPrimary, Yxy<O> greenPrimary, Yxy<O> bluePrimary,
      @Arguments.Nullable Curve gammaCurve) {
    RGBToXYZ<I, O> rgbToXYZ = RGBToXYZ
        .newRGBToXYZ(rgbSpace, whitepoint, redPrimary, greenPrimary, bluePrimary, gammaCurve);
    return rgbToXYZ.inverse();
  }

  public FixedMatrix3x3_64F getXYZToLinearRGB() {
    return xyzToLinearRGB.copy();
  }

  public Curve getInverseGammaCurve() {
    return invGammaCurve;
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
    return t.inverse.equals(inverse);
  }

  @Override
  public int hashCode() {
    return XYZToRGB.class.hashCode() ^ inverse.hashCode();
  }

  @Override
  public RGBToXYZ<O, I> inverse() {
    return inverse;
  }

  @Override
  public I getInputSpace() {
    return inverse.getOutputSpace();
  }

  @Override
  public O getOutputSpace() {
    return inverse.getInputSpace();
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    FixedMatrix3_64F in = new FixedMatrix3_64F(input[0], input[1], input[2]);
    FixedMatrix3_64F out = new FixedMatrix3_64F();

    // Transform from XYZ to linear RGB
    FixedOps3.mult(xyzToLinearRGB, in, out);

    // Apply gamma correction
    if (invGammaCurve != null) {
      output[0] = invGammaCurve.evaluate(clampToCurveDomain(out.a1));
      output[1] = invGammaCurve.evaluate(clampToCurveDomain(out.a2));
      output[2] = invGammaCurve.evaluate(clampToCurveDomain(out.a3));
    } else {
      output[0] = out.a1;
      output[1] = out.a2;
      output[2] = out.a3;
    }
    return true;
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

  private double clampToCurveDomain(double c) {
    // Only called when invGammaCurve is not null
    return Functions.clamp(c, invGammaCurve.getDomainMin(), invGammaCurve.getDomainMax());
  }
}
