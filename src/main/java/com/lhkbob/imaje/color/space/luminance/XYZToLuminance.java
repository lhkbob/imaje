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
package com.lhkbob.imaje.color.space.luminance;

import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * XYZToLuminance
 * ==============
 *
 * Color transform between {@link CIE31} {@link XYZ} and {@link Linear} {@link Luminance}.
 *
 * @author Michael Ludwig
 */
public class XYZToLuminance implements Transform<XYZ<CIE31>, CIE31, Luminance<Linear>, Linear> {
  private final Linear lumSpace;
  private final XYZ<CIE31> whitepoint; // cached from lumSpace
  private final LuminanceToXYZ inverse;

  /**
   * Create a transformation for the given Linear Luminance space, using its declared whitepoint
   * for the conversion process.
   *
   * @param lumSpace
   *     The color space
   */
  public XYZToLuminance(Linear lumSpace) {
    this.lumSpace = lumSpace;
    whitepoint = lumSpace.getReferenceWhitepoint();
    inverse = new LuminanceToXYZ(this);
  }

  XYZToLuminance(LuminanceToXYZ inverse) {
    lumSpace = inverse.getInputSpace();
    whitepoint = lumSpace.getReferenceWhitepoint();
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToLuminance)) {
      return false;
    }
    return Objects.equals(((XYZToLuminance) o).lumSpace, lumSpace);
  }

  @Override
  public int hashCode() {
    return XYZToLuminance.class.hashCode() ^ lumSpace.hashCode();
  }

  @Override
  public LuminanceToXYZ inverse() {
    return inverse();
  }

  @Override
  public CIE31 getInputSpace() {
    return CIE31.SPACE;
  }

  @Override
  public Linear getOutputSpace() {
    return lumSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 1, output.length);

    output[0] = input[1] / whitepoint.y();

    return true;
  }

  @Override
  public String toString() {
    return String.format("XYZ -> Luminance Transform (whitepoint: %s)", whitepoint);
  }
}
