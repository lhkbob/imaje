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

import static com.lhkbob.imaje.color.transform.general.XYZToHunterLab.calculateKA;
import static com.lhkbob.imaje.color.transform.general.XYZToHunterLab.calculateKB;

/**
 *
 */
public class HunterLabToXYZ implements Transform {
  private final double invKA;
  private final double invKB;
  private final XYZ whitepoint;

  public HunterLabToXYZ(XYZ whitepoint) {
    this(whitepoint, false);
  }

  HunterLabToXYZ(XYZ whitepoint, boolean ownWhite) {
    this.whitepoint = (ownWhite ? whitepoint : whitepoint.clone());
    invKA = 1.0 / calculateKA(whitepoint);
    invKB = 1.0 / calculateKB(whitepoint);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof HunterLabToXYZ)) {
      return false;
    }
    return ((HunterLabToXYZ) o).whitepoint.equals(whitepoint);
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public HunterLabToXYZ getLocallySafeInstance() {
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
  public XYZToHunterLab inverted() {
    return new XYZToHunterLab(whitepoint, true);
  }

  @Override
  public String toString() {
    return String.format("Hunter Lab -> XYZ (whitepoint: %s)", whitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double rootYP = input[0] / 100.0;
    double yp = rootYP * rootYP;
    double xp = input[1] * rootYP * invKA + yp;
    double zp = yp - input[2] * rootYP * invKB;

    output[0] = xp * whitepoint.x();
    output[1] = yp * whitepoint.y();
    output[2] = zp * whitepoint.z();
  }
}
