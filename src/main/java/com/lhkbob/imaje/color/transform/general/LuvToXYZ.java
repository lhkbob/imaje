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

import static com.lhkbob.imaje.color.transform.general.CIELabToXYZ.L_SCALE;
import static com.lhkbob.imaje.color.transform.general.CIELabToXYZ.inverseF;

/**
 *
 */
public class LuvToXYZ implements Transform {
  private final XYZ referenceWhitepoint;
  private final double uWhite, vWhite;

  public LuvToXYZ(XYZ referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
    uWhite = XYZToLuv
        .uPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
    vWhite = XYZToLuv
        .vPrime(referenceWhitepoint.x(), referenceWhitepoint.y(), referenceWhitepoint.z());
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof LuvToXYZ)) {
      return false;
    }
    return ((LuvToXYZ) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public LuvToXYZ getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public int hashCode() {
    return referenceWhitepoint.hashCode();
  }

  @Override
  public XYZToLuv inverted() {
    return new XYZToLuv(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("L*u*v* -> XYZ Transform (whitepoint: %s)", referenceWhitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double up = input[1] / (13.0 * input[0]) + uWhite;
    double vp = input[2] / (13.0 * input[0]) + vWhite;

    // Y from L*
    output[1] = referenceWhitepoint.y() * inverseF(L_SCALE * (input[0] + 16.0));
    double denom = 9.0 * output[1] / vp;
    // X from Y, up, and denom
    output[0] = 0.25 * up * denom;
    // Z from Y, X, and denom
    output[2] = (denom - output[0] - 15.0 * output[1]) / 3.0;
  }
}
