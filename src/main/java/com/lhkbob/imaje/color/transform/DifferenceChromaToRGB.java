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
package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.YCbCr;
import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.color.space.yuv.YCbCrSpace;
import com.lhkbob.imaje.color.space.yuv.YUVSpace;
import com.lhkbob.imaje.util.Arguments;

/**
 * YCbCr's range is [0, 1], [-0.5, 0.5], [-0.5, 0.5]
 */
public class DifferenceChromaToRGB<SI extends ColorSpace<I, SI>, I extends Color<I, SI>, SO extends ColorSpace<RGB<SO>, SO>> implements ColorTransform<SI, I, SO, RGB<SO>> {
  private final SI yCbCrSpace;
  private final SO rgbSpace;

  final double kb;
  final double kr;
  final double umax;
  final double vmax;

  private final RGBToDifferenceChroma<SO, SI, I> inverse;

  private DifferenceChromaToRGB(SI yCbCrSpace, SO rgbSpace, double kb, double kr, double umax, double vmax) {
    Arguments.notNull("yCbCrSpace", yCbCrSpace);
    Arguments.notNull("rgbSpace", rgbSpace);

    this.yCbCrSpace = yCbCrSpace;
    this.rgbSpace = rgbSpace;

    this.kr = kr;
    this.kb = kb;
    this.umax = umax;
    this.vmax = vmax;

    inverse = new RGBToDifferenceChroma<>(this);
  }

  public static <S extends ColorSpace<RGB<S>, S>> DifferenceChromaToRGB<YCbCrSpace<S>, YCbCr<S>, S> newYCbCrToRGB(YCbCrSpace<S> yCbCrSpace, double kb, double kr) {
    return new DifferenceChromaToRGB<>(yCbCrSpace, yCbCrSpace.getRGBSpace(), kb, kr, 0.5, 0.5);
  }

  public static <S extends ColorSpace<RGB<S>, S>> DifferenceChromaToRGB<YUVSpace<S>, YUV<S>, S> newYUVToRGB(
      YUVSpace<S> yuvSpace, double kb, double kr) {
    return new DifferenceChromaToRGB<>(yuvSpace, yuvSpace.getRGBSpace(), kb, kr, 0.436, 0.615);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof DifferenceChromaToRGB)) {
      return false;
    }
    DifferenceChromaToRGB c = (DifferenceChromaToRGB) o;
    if (!c.yCbCrSpace.equals(yCbCrSpace) || !c.rgbSpace.equals(rgbSpace))
      return false;
    return Double.compare(c.kr, kr) == 0 && Double.compare(c.kb, kb) == 0
        && Double.compare(c.umax, umax) == 0 && Double.compare(c.vmax, vmax) == 0;
  }

  @Override
  public int hashCode() {
    int result = DifferenceChromaToRGB.class.hashCode();
    result = 31 * result + Double.hashCode(kr);
    result = 31 * result + Double.hashCode(kb);
    result = 31 * result + Double.hashCode(umax);
    result = 31 * result + Double.hashCode(vmax);
    result = 31 * result + yCbCrSpace.hashCode();
    result = 31 * result + rgbSpace.hashCode();
    return result;
  }

  @Override
  public RGBToDifferenceChroma<SO, SI, I> inverse() {
    return inverse;
  }

  @Override
  public SI getInputSpace() {
    return yCbCrSpace;
  }

  @Override
  public SO getOutputSpace() {
    return rgbSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    // R from Y and Cr
    output[0] = (1.0 - kr) * input[0] * input[2] / vmax;
    // B from Y and Cb
    output[2] = (1.0 - kb) * input[0] * input[1] / vmax;
    // G from Y, R, and B
    output[1] = (input[0] - kr * output[0] - kb * output[2]) / (1.0 - kr - kb);
    return true;
  }

  @Override
  public String toString() {
    return String
        .format("Yb*r* -> RGB (kb: %.3f, kr: %.3f, b-max: %.3f, r-max: %.3f)", kb, kr, umax, vmax);
  }
}
