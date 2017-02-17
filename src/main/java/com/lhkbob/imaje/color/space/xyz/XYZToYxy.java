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
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * XYZToYxy
 * ========
 *
 * Color transformation from {@link XYZ} to {@link Yxy}.
 *
 * @author Michael Ludwig
 */
public class XYZToYxy<S extends ColorSpace<XYZ<S>, S>> implements Transform<XYZ<S>, S, Yxy<S>, YxySpace<S>> {
  private final YxySpace<S> outputSpace;
  private final YxyToXYZ<S> inverse;

  /**
   * Create a transformation for the given YxySpace.
   *
   * @param outputSpace
   *     The output space of this transformation (implicitly defines the input space)
   */
  public XYZToYxy(YxySpace<S> outputSpace) {
    Arguments.notNull("outputSpace", outputSpace);

    this.outputSpace = outputSpace;
    inverse = new YxyToXYZ<>(this);
  }

  XYZToYxy(YxyToXYZ<S> inverse) {
    outputSpace = inverse.getInputSpace();
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof XYZToYxy)) {
      return false;
    }
    return Objects.equals(((XYZToYxy<?>) o).outputSpace, outputSpace);
  }

  @Override
  public int hashCode() {
    return XYZToYxy.class.hashCode() ^ outputSpace.hashCode();
  }

  @Override
  public YxyToXYZ<S> inverse() {
    return inverse;
  }

  @Override
  public S getInputSpace() {
    return outputSpace.getXYZSpace();
  }

  @Override
  public YxySpace<S> getOutputSpace() {
    return outputSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    double sum = input[0] + input[1] + input[2];
    // Y from Y
    output[0] = input[1];
    // x from X, Y, and Z
    output[1] = input[0] / sum;
    // y from X, Y, and Z
    output[2] = input[1] / sum;

    return true;
  }

  @Override
  public String toString() {
    return "XYZ -> Yxy Transform";
  }
}
