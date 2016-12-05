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
package com.lhkbob.imaje.color.space.lab;

import com.lhkbob.imaje.color.CIELUV;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.util.Arguments;

/**
 * XYZToCIELUV
 * ===========
 *
 * Color transformation from the {@link CIE31} {@link XYZ} space to {@link CIELUV}.
 *
 * @author Michael Ludwig
 */
public class XYZToCIELUV implements ColorTransform<CIE31, XYZ<CIE31>, CIELUVSpace, CIELUV> {
  private final XYZ<CIE31> referenceWhitepoint; // cached from luvSpace
  private final double uWhite, vWhite;
  private final CIELUVToXYZ inverse;
  private final CIELUVSpace luvSpace;

  /**
   * Create a new transformation.
   *
   * @param luvSpace
   *     The LUV space
   */
  public XYZToCIELUV(CIELUVSpace luvSpace) {
    this.luvSpace = luvSpace;
    this.referenceWhitepoint = luvSpace.getReferenceWhitepoint();
    uWhite = uPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    vWhite = vPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    inverse = new CIELUVToXYZ(this);
  }

  XYZToCIELUV(CIELUVToXYZ inverse) {
    luvSpace = inverse.getInputSpace();
    referenceWhitepoint = luvSpace.getReferenceWhitepoint();
    uWhite = uPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    vWhite = vPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToCIELUV)) {
      return false;
    }
    return ((XYZToCIELUV) o).luvSpace.equals(luvSpace);
  }

  @Override
  public int hashCode() {
    return XYZToCIELUV.class.hashCode() ^ luvSpace.hashCode();
  }

  @Override
  public CIELUVToXYZ inverse() {
    return inverse;
  }

  @Override
  public CIE31 getInputSpace() {
    return CIE31.SPACE;
  }

  @Override
  public CIELUVSpace getOutputSpace() {
    return luvSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    double up = uPrime(input[0], input[1], input[2]);
    double vp = vPrime(input[0], input[1], input[2]);
    output[0] = 116.0 * XYZToCIELAB.f(input[1] / referenceWhitepoint.y()) - 16.0; // L*
    output[1] = 13.0 * output[0] * (up - uWhite); // u*
    output[2] = 13.0 * output[0] * (vp - vWhite); // v*

    return true;
  }

  @Override
  public String toString() {
    return String.format("XYZ -> L*u*v* Transform (whitepoint: %s)", referenceWhitepoint);
  }

  static double uPrime(double x, double y, double z) {
    return 4.0 * x / (x + 15.0 * y + 3.0 * z);
  }

  static double vPrime(double x, double y, double z) {
    return 9.0 * y / (x + 15.0 * y + 3.0 * z);
  }
}
