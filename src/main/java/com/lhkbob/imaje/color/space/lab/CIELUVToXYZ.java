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
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;
import java.util.Optional;

/**
 * CIELUVToXYZ
 * ===========
 *
 * Color transformation from {@link CIELUV} to the {@link CIE31} {@link XYZ} space.
 *
 * @author Michael Ludwig
 */
public class CIELUVToXYZ implements Transform<CIELUV, CIELUVSpace, XYZ<CIE31>, CIE31> {
  private final XYZ<CIE31> referenceWhitepoint; // cached from luvSpace
  private final double uWhite, vWhite;
  private final XYZToCIELUV inverse;
  private final CIELUVSpace luvSpace;

  /**
   * Create a new transformation.
   *
   * @param luvSpace
   *     The LUV space
   */
  public CIELUVToXYZ(CIELUVSpace luvSpace) {
    this.luvSpace = luvSpace;
    this.referenceWhitepoint = luvSpace.getReferenceWhitepoint();

    uWhite = XYZToCIELUV
        .uPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    vWhite = XYZToCIELUV
        .vPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());

    inverse = new XYZToCIELUV(this);
  }

  CIELUVToXYZ(XYZToCIELUV inverse) {
    luvSpace = inverse.getOutputSpace();
    referenceWhitepoint = luvSpace.getReferenceWhitepoint();

    uWhite = XYZToCIELUV
        .uPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    vWhite = XYZToCIELUV
        .vPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CIELUVToXYZ)) {
      return false;
    }
    return Objects.equals(((CIELUVToXYZ) o).luvSpace, luvSpace);
  }

  @Override
  public int hashCode() {
    return CIELUVToXYZ.class.hashCode() ^ luvSpace.hashCode();
  }

  @Override
  public Optional<XYZToCIELUV> inverse() {
    return Optional.of(inverse);
  }

  @Override
  public CIELUVSpace getInputSpace() {
    return luvSpace;
  }

  @Override
  public CIE31 getOutputSpace() {
    return CIE31.SPACE;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    double up = input[1] / (13.0 * input[0]) + uWhite;
    double vp = input[2] / (13.0 * input[0]) + vWhite;

    // Y from L*
    output[1] = referenceWhitepoint.y() * CIELABToXYZ.inverseF(CIELABToXYZ.L_SCALE * (input[0] + 16.0));
    double denom = 9.0 * output[1] / vp;
    // X from Y, up, and denom
    output[0] = 0.25 * up * denom;
    // Z from Y, X, and denom
    output[2] = (denom - output[0] - 15.0 * output[1]) / 3.0;

    return true;
  }

  @Override
  public String toString() {
    return String.format("L*u*v* -> XYZ Transform (whitepoint: %s)", referenceWhitepoint);
  }
}
