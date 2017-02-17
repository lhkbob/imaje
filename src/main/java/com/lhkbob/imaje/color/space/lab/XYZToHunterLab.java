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
 * XYZToHunterLab
 * ==============
 *
 * Color transformation from the {@link CIE31} {@link XYZ} space to the {@link Hunter} {@link Lab}
 * space.
 *
 * @author Michael Ludwig
 */
public class XYZToHunterLab implements Transform<XYZ<CIE31>, CIE31, Lab<Hunter>, Hunter> {
  private final double ka;
  private final double kb;
  private final XYZ<CIE31> whitepoint; // cached from labSpace
  private final Hunter labSpace;
  private final HunterLabToXYZ inverse;

  /**
   * Create a new transformation.
   *
   * @param labSpace
   *     The Hunter Lab space
   */
  public XYZToHunterLab(Hunter labSpace) {
    this.labSpace = labSpace;
    this.whitepoint = labSpace.getReferenceWhitepoint();
    ka = calculateKA(whitepoint);
    kb = calculateKB(whitepoint);

    inverse = new HunterLabToXYZ(this);
  }

  XYZToHunterLab(HunterLabToXYZ inverse) {
    labSpace = inverse.getInputSpace();
    whitepoint = labSpace.getReferenceWhitepoint();
    ka = calculateKA(whitepoint);
    kb = calculateKB(whitepoint);
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToHunterLab)) {
      return false;
    }
    return Objects.equals(((XYZToHunterLab) o).whitepoint, whitepoint);
  }

  @Override
  public int hashCode() {
    return XYZToHunterLab.class.hashCode() ^ whitepoint.hashCode();
  }

  @Override
  public HunterLabToXYZ inverse() {
    return inverse;
  }

  @Override
  public CIE31 getInputSpace() {
    return CIE31.SPACE;
  }

  @Override
  public Hunter getOutputSpace() {
    return labSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    double xp = input[0] / whitepoint.x();
    double yp = input[1] / whitepoint.y();
    double rootYP = Math.sqrt(yp);
    double zp = input[2] / whitepoint.z();

    output[0] = 100.0 * rootYP;
    output[1] = ka * (xp - yp) / rootYP;
    output[2] = kb * (yp - zp) / rootYP;

    return true;
  }

  @Override
  public String toString() {
    return String.format("XYZ -> Hunter Lab (whitepoint: %s)", whitepoint);
  }

  static double calculateKA(XYZ<CIE31> whitepoint) {
    return 175.0 / 198.04 * (whitepoint.x() + whitepoint.y());
  }

  static double calculateKB(XYZ<CIE31> whitepoint) {
    return 70.0 / 218.11 * (whitepoint.y() + whitepoint.z());
  }
}
