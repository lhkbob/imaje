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

/**
 *
 */
public class CIELabToXYZ implements Transform {
  private final XYZ referenceWhitepoint;
  public CIELabToXYZ(XYZ referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CIELabToXYZ)) {
      return false;
    }
    return ((CIELabToXYZ) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public CIELabToXYZ getLocallySafeInstance() {
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
  public XYZToCIELab inverted() {
    return new XYZToCIELab(referenceWhitepoint);
  }

  @Override
  public String toString() {
    return String.format("CIELAB -> XYZ Transform (whitepoint: %s)", referenceWhitepoint);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double lp = L_SCALE * (input[0] + 16.0);
    // X from L and a
    output[0] = referenceWhitepoint.x() * inverseF(lp + A_SCALE * input[1]);
    // Y from L
    output[1] = referenceWhitepoint.y() * inverseF(lp);
    // Z from L and b
    output[2] = referenceWhitepoint.z() * inverseF(lp - B_SCALE * input[2]);
  }

  static double inverseF(double t) {
    if (t > LINEAR_THRESHOLD) {
      return t * t * t;
    } else {
      return LINEAR_SLOPE * (t - LINEAR_OFFSET);
    }
  }
  static final double A_SCALE = 1.0 / 500.0;
  static final double B_SCALE = 1.0 / 200.0;
  static final double LINEAR_OFFSET = 4.0 / 29.0; // ~0.138
  static final double LINEAR_THRESHOLD = 6.0 / 29.0;
  static final double LINEAR_SLOPE = 3.0 * LINEAR_THRESHOLD * LINEAR_THRESHOLD;
  static final double L_SCALE = 1.0 / 116.0;
}
