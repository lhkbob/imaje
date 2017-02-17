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
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import org.ejml.alg.fixed.FixedOps3;
import org.ejml.data.FixedMatrix3_64F;
import org.ejml.data.FixedMatrix3x3_64F;

import java.util.Objects;

/**
 * XYZToRGB
 * ========
 *
 * Color transform from {@link XYZ} to {@link RGB}, described by a 3x3 linear transformation and an
 * encoding gamma function applied to each channel.
 *
 * @author Michael Ludwig
 */
public class XYZToRGB<I extends ColorSpace<XYZ<I>, I>, O extends ColorSpace<RGB<O>, O>> implements Transform<XYZ<I>, I, RGB<O>, O> {
  private final Curve encodingGammaCurve;
  private final RGBToXYZ<O, I> inverse;
  private final O rgbSpace;
  private final I xyzSpace;
  private final FixedMatrix3x3_64F xyzToLinearRGB;

  /**
   * Create a new XYZToRGB transform between the XYZ and RGB spaces. The transformation is
   * the composition of the 3x3 matrix, `xyzToLinear`, and then applying `encodingGamma` to each
   * of the resulting linear RGB channels. If either `xyzToLinear` or `encodingGamma` are not
   * invertable, then {@link #inverse()} will return `null`.
   *
   * `encodingGamma` can be null to skip that step and keep the coordinates in a linear space.
   *
   * @param xyzSpace The XYZ space
   * @param rgbSpace The RGB space
   * @param xyzToLinear The linear transformation from XYZ to linear RGB
   * @param encodingGamma The optional gamma encoding from linear to non-linear RGB
   */
  public XYZToRGB(
      I xyzSpace, O rgbSpace, FixedMatrix3x3_64F xyzToLinear,
      @Arguments.Nullable Curve encodingGamma) {
    Arguments.notNull("xyzSpace", xyzSpace);
    Arguments.notNull("rgbSpace", rgbSpace);
    this.xyzSpace = xyzSpace;
    this.rgbSpace = rgbSpace;
    xyzToLinearRGB = xyzToLinear.copy();
    encodingGammaCurve = encodingGamma;

    // Calculate inverse
    FixedMatrix3x3_64F rgbToXYZ = new FixedMatrix3x3_64F();
    if (FixedOps3.invert(xyzToLinear, rgbToXYZ)) {
      // Try and invert the gamma function
      if (encodingGamma == null) {
        // There is no gamma to invert so inverse is available
        inverse = new RGBToXYZ<>(this, rgbToXYZ, null);
      } else {
        Curve decodingGamma = encodingGamma.inverted();
        if (decodingGamma != null) {
          inverse = new RGBToXYZ<>(this, rgbToXYZ, decodingGamma);
        } else {
          inverse = null;
        }
      }
    } else {
      // Could not invert the 3x3 linear conversion, so inverse is not available
      inverse = null;
    }
  }

  XYZToRGB(
      RGBToXYZ<O, I> inverse, FixedMatrix3x3_64F xyzToLinearRGB,
      @Arguments.Nullable Curve encodingGammaCurve) {
    xyzSpace = inverse.getOutputSpace();
    rgbSpace = inverse.getInputSpace();
    this.inverse = inverse;
    this.xyzToLinearRGB = xyzToLinearRGB;
    this.encodingGammaCurve = encodingGammaCurve;
  }

  /**
   * Create a new transformation from XYZ to RGB based on the provided whitepoint and tristimulus
   * values the for red, green, and blue primaries of the space. While it possible to create a
   * transformation for an RGB space with non-standard primaries, that should be avoided. RGB
   * space definitions should use the primary and whitepoint that are defined in their standard
   * so that the transformation is meaningful.
   *
   * The transformation between XYZ and RGB is calculated by forming a matrix from the chromaticity
   * coordinates of the primaries (including the `z` chromaticity). This matrix is then scaled so
   * that `matrix * whitepoint = [1,1,1]`. The luminance of the primaries is ignored for this
   * purpose; it depends on their chromaticities and the luminance of the whitepoint.
   *
   * The transformation can include a decoding gamma transformation from the non-linear RGB
   * coordinates to linear RGB. `null` can be provided for this function to denote the identity
   * transformation.
   *
   * Calling this factory method is equivalent to calling {@link RGBToXYZ#newRGBToXYZ(ColorSpace,
   * XYZ, Yxy, Yxy, Yxy, Curve)} and then returning its inverse, if the inverse exists. If there is
   * no such inverse given the gamma curve and primaries then an exception is thrown.
   *
   * @param rgbSpace
   *     The RGB space of the transformation
   * @param whitepoint
   *     The whitepoint defining the transformation
   * @param redPrimary
   *     The chromaticity coordinates of the red primary (ignores `Y`)
   * @param greenPrimary
   *     The chromaticity coordinates of the green primary (ignores `Y`)
   * @param bluePrimary
   *     The chromaticity coordinates of the blue primary (ignores `Y`)
   * @param gammaCurve
   *     The decoding gamma curve from non-linear to linear RGB
   * @param <I>
   *     The RGB space
   * @param <O>
   *     The XYZ space (determined by `whitepoint`)
   * @throws IllegalArgumentException
   *     if the the primaries and gamma curve do not form
   *     a computabe XYZ to RGB transformation (e.g. when the RGB to XYZ transform is not
   *     invertable).
   */
  public static <I extends ColorSpace<RGB<I>, I>, O extends ColorSpace<XYZ<O>, O>> XYZToRGB<O, I> newXYZToRGB(
      I rgbSpace, XYZ<O> whitepoint, Yxy<O> redPrimary, Yxy<O> greenPrimary, Yxy<O> bluePrimary,
      @Arguments.Nullable Curve gammaCurve) {
    RGBToXYZ<I, O> rgbToXYZ = RGBToXYZ
        .newRGBToXYZ(rgbSpace, whitepoint, redPrimary, greenPrimary, bluePrimary, gammaCurve);
    XYZToRGB<O, I> xyzToRGB = rgbToXYZ.inverse();
    if (xyzToRGB == null) {
      throw new IllegalArgumentException(
          "Primaries, whitepoint, and gamma curve do not create a valid XYZ to RGB conversion");
    }
    return xyzToRGB;
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
    if (encodingGammaCurve != null) {
      output[0] = encodingGammaCurve.evaluate(clampToCurveDomain(out.a1));
      output[1] = encodingGammaCurve.evaluate(clampToCurveDomain(out.a2));
      output[2] = encodingGammaCurve.evaluate(clampToCurveDomain(out.a3));
    } else {
      output[0] = out.a1;
      output[1] = out.a2;
      output[2] = out.a3;
    }
    return true;
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
    if (!Objects.equals(t.xyzSpace, xyzSpace) || !Objects.equals(t.rgbSpace, rgbSpace)) {
      return false;
    }
    if (!Objects.equals(t.encodingGammaCurve, encodingGammaCurve)) {
      return false;
    }

    return Double.compare(t.xyzToLinearRGB.a11, xyzToLinearRGB.a11) == 0
        && Double.compare(t.xyzToLinearRGB.a12, xyzToLinearRGB.a12) == 0
        && Double.compare(t.xyzToLinearRGB.a13, xyzToLinearRGB.a13) == 0
        && Double.compare(t.xyzToLinearRGB.a21, xyzToLinearRGB.a21) == 0
        && Double.compare(t.xyzToLinearRGB.a22, xyzToLinearRGB.a22) == 0
        && Double.compare(t.xyzToLinearRGB.a23, xyzToLinearRGB.a23) == 0
        && Double.compare(t.xyzToLinearRGB.a31, xyzToLinearRGB.a31) == 0
        && Double.compare(t.xyzToLinearRGB.a32, xyzToLinearRGB.a32) == 0
        && Double.compare(t.xyzToLinearRGB.a33, xyzToLinearRGB.a33) == 0;
  }

  /**
   * @return The gamma function that encodes linear to non-linear values, or null if there is no
   * gamma conversion
   */
  public Curve getEncodingGammaFunction() {
    return encodingGammaCurve;
  }

  @Override
  public I getInputSpace() {
    return xyzSpace;
  }

  @Override
  public O getOutputSpace() {
    return rgbSpace;
  }

  /**
   * @return The 3x3 transformation from XYZ tristimulus to linear RGB values.
   */
  public FixedMatrix3x3_64F getXYZToLinearRGB() {
    return xyzToLinearRGB.copy();
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
    result = 31 * result + (encodingGammaCurve != null ? encodingGammaCurve.hashCode() : 0);
    result = 31 * result + xyzSpace.hashCode();
    result = 31 * result + rgbSpace.hashCode();
    return result;
  }

  @Override
  public RGBToXYZ<O, I> inverse() {
    return inverse;
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

    if (encodingGammaCurve != null) {
      sb.append("\n  gamma correct: ").append(encodingGammaCurve);
    }
    return sb.toString();
  }

  private double clampToCurveDomain(double c) {
    // Only called when encodingGammaCurve is not null
    return Functions.clamp(c, encodingGammaCurve.getDomainMin(), encodingGammaCurve.getDomainMax());
  }
}
