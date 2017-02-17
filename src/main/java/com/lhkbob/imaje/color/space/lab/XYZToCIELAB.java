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

import com.lhkbob.imaje.color.Lab;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * XYZToCIELAB
 * ===========
 *
 * Color transformation from the {@link CIE31} {@link XYZ} space to the {@link CIE} {@link Lab}
 * space.
 *
 * @author Michael Ludwig
 */
public class XYZToCIELAB implements Transform<XYZ<CIE31>, CIE31, Lab<CIE>, CIE> {
  private final XYZ<CIE31> referenceWhitepoint; // cached from labSpace
  private final CIE labSpace;
  private final CIELABToXYZ inverse;

  /**
   * Create a new transformation.
   *
   * @param labSpace
   *     The CIE Lab space
   */
  public XYZToCIELAB(CIE labSpace) {
    this.referenceWhitepoint = labSpace.getReferenceWhitepoint();
    this.labSpace = labSpace;
    inverse = new CIELABToXYZ(this);
  }

  XYZToCIELAB(CIELABToXYZ inverse) {
    labSpace = inverse.getInputSpace();
    referenceWhitepoint = labSpace.getReferenceWhitepoint();
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToCIELAB)) {
      return false;
    }
    return Objects.equals(((XYZToCIELAB) o).labSpace, labSpace);
  }

  @Override
  public int hashCode() {
    return XYZToCIELAB.class.hashCode() ^ labSpace.hashCode();
  }

  @Override
  public CIELABToXYZ inverse() {
    return inverse;
  }

  @Override
  public CIE31 getInputSpace() {
    return CIE31.SPACE;
  }

  @Override
  public CIE getOutputSpace() {
    return labSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    double fx = f(input[0] / referenceWhitepoint.x());
    double fy = f(input[1] / referenceWhitepoint.y());
    double fz = f(input[2] / referenceWhitepoint.z());

    output[0] = 116.0 * fy - 16.0; // L*
    output[1] = 500 * (fx - fy); // a*
    output[2] = 200 * (fy - fz); // b*

    return true;
  }

  @Override
  public String toString() {
    return String.format("XYZ -> CIELAB Transform (whitepoint: %s)", referenceWhitepoint);
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
