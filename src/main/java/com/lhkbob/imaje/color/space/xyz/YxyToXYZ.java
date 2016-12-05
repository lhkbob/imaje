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
package com.lhkbob.imaje.color.space.xyz;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * YxyToXYZ
 * ========
 *
 * Color transformation from {@link Yxy} to {@link XYZ}.
 *
 * @author Michael Ludwig
 */
public class YxyToXYZ<S extends ColorSpace<XYZ<S>, S>> implements ColorTransform<YxySpace<S>, Yxy<S>, S, XYZ<S>> {
  private final YxySpace<S> inputSpace;
  private final XYZToYxy<S> inverse;

  /**
   * Create a new transformation for the given YxySpace.
   *
   * @param inputSpace
   *     The input space of this transformation (implicitly defines the output space)
   */
  public YxyToXYZ(YxySpace<S> inputSpace) {
    Arguments.notNull("inputSpace", inputSpace);

    this.inputSpace = inputSpace;
    inverse = new XYZToYxy<>(this);
  }

  YxyToXYZ(XYZToYxy<S> inverse) {
    inputSpace = inverse.getOutputSpace();
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof YxyToXYZ)) {
      return false;
    }
    return Objects.equals(((YxyToXYZ<?>) o).inputSpace, inputSpace);
  }

  @Override
  public int hashCode() {
    return YxyToXYZ.class.hashCode() ^ inputSpace.hashCode();
  }

  @Override
  public XYZToYxy<S> inverse() {
    return inverse;
  }

  @Override
  public YxySpace<S> getInputSpace() {
    return inputSpace;
  }

  @Override
  public S getOutputSpace() {
    return inputSpace.getXYZSpace();
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    // X from Y, x, and y
    output[0] = input[0] * input[1] / input[2];
    // Y from Y
    output[1] = input[0];
    // Z from Y, y, and x
    output[2] = input[0] * (1.0 - input[1] - input[2]) / input[2];
    return true;
  }

  @Override
  public String toString() {
    return "Yxy -> XYZ Transform";
  }
}
