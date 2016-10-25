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
package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class XYZToHunterLab implements Transform {
  private final double ka;
  private final double kb;
  private final XYZ whitepoint;

  public XYZToHunterLab(XYZ whitepoint) {
    this(whitepoint, false);
  }

  XYZToHunterLab(XYZ whitepoint, boolean ownWhite) {
    this.whitepoint = (ownWhite ? whitepoint : whitepoint.clone());
    ka = calculateKA(whitepoint);
    kb = calculateKB(whitepoint);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToHunterLab)) {
      return false;
    }
    return ((XYZToHunterLab) o).whitepoint.equals(whitepoint);
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public XYZToHunterLab getLocallySafeInstance() {
    return this;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    return whitepoint.hashCode();
  }

  @Override
  public HunterLabToXYZ inverted() {
    return new HunterLabToXYZ(whitepoint, true);
  }

  @Override
  public String toString() {
    return String.format("XYZ -> Hunter Lab (whitepoint: %s)", whitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double xp = input[0] / whitepoint.x();
    double yp = input[1] / whitepoint.y();
    double rootYP = Math.sqrt(yp);
    double zp = input[2] / whitepoint.z();

    output[0] = 100.0 * rootYP;
    output[1] = ka * (xp - yp) / rootYP;
    output[2] = kb * (yp - zp) / rootYP;
  }

  static double calculateKA(XYZ whitepoint) {
    return 175.0 / 198.04 * (whitepoint.x() + whitepoint.y());
  }

  static double calculateKB(XYZ whitepoint) {
    return 70.0 / 218.11 * (whitepoint.y() + whitepoint.z());
  }
}
