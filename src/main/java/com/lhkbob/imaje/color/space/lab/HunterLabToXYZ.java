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
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.util.Arguments;

/**
 * HunterLabToXYZ
 * ==============
 *
 * Color transformation from the {@link Hunter} {@link Lab} to the {@link CIE31} {@link XYZ} space.
 *
 * @author Michael Ludwig
 */
public class HunterLabToXYZ implements ColorTransform<Hunter, Lab<Hunter>, CIE31, XYZ<CIE31>> {
  private final double invKA;
  private final double invKB;
  private final XYZ<CIE31> whitepoint; // cached from labSpace

  private final Hunter labSpace;
  private final XYZToHunterLab inverse;

  /**
   * Create a new transformation.
   *
   * @param labSpace
   *     The Hunter Lab space
   */
  public HunterLabToXYZ(Hunter labSpace) {
    this.labSpace = labSpace;
    this.whitepoint = labSpace.getReferenceWhitepoint();
    invKA = 1.0 / XYZToHunterLab.calculateKA(whitepoint);
    invKB = 1.0 / XYZToHunterLab.calculateKB(whitepoint);

    inverse = new XYZToHunterLab(this);
  }

  HunterLabToXYZ(XYZToHunterLab inverse) {
    labSpace = inverse.getOutputSpace();
    whitepoint = labSpace.getReferenceWhitepoint();
    invKA = 1.0 / XYZToHunterLab.calculateKA(whitepoint);
    invKB = 1.0 / XYZToHunterLab.calculateKB(whitepoint);

    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof HunterLabToXYZ)) {
      return false;
    }
    return ((HunterLabToXYZ) o).labSpace.equals(labSpace);
  }

  @Override
  public int hashCode() {
    return HunterLabToXYZ.class.hashCode() ^ labSpace.hashCode();
  }

  @Override
  public XYZToHunterLab inverse() {
    return inverse;
  }

  @Override
  public Hunter getInputSpace() {
    return labSpace;
  }

  @Override
  public CIE31 getOutputSpace() {
    return CIE31.SPACE;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    double rootYP = input[0] / 100.0;
    double yp = rootYP * rootYP;
    double xp = input[1] * rootYP * invKA + yp;
    double zp = yp - input[2] * rootYP * invKB;

    output[0] = xp * whitepoint.x();
    output[1] = yp * whitepoint.y();
    output[2] = zp * whitepoint.z();

    return true;
  }

  @Override
  public String toString() {
    return String.format("Hunter Lab -> XYZ (whitepoint: %s)", whitepoint);
  }
}
