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
 *
 */
public class RGBToDifferenceChroma<SI extends ColorSpace<RGB<SI>, SI>, SO extends ColorSpace<O, SO>, O extends Color<O, SO>> implements ColorTransform<SI, RGB<SI>, SO, O> {
  private final DifferenceChromaToRGB<SO, O, SI> inverse;

  RGBToDifferenceChroma(DifferenceChromaToRGB<SO, O, SI> inverse) {
    this.inverse = inverse;
  }

  public static <S extends ColorSpace<RGB<S>, S>> RGBToDifferenceChroma<S, YCbCrSpace<S>, YCbCr<S>> newRGBToYCbCr(
      YCbCrSpace<S> yCbCrSpace, double kb, double kr) {
    return DifferenceChromaToRGB.newYCbCrToRGB(yCbCrSpace, kb, kr).inverse();
  }

  public static <S extends ColorSpace<RGB<S>, S>> RGBToDifferenceChroma<S, YUVSpace<S>, YUV<S>> newYUVToRGB(
      YUVSpace<S> yuvSpace, double kb, double kr) {
    return DifferenceChromaToRGB.newYUVToRGB(yuvSpace, kb, kr).inverse();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof RGBToDifferenceChroma)) {
      return false;
    }
    RGBToDifferenceChroma c = (RGBToDifferenceChroma) o;
    return c.inverse.equals(inverse);
  }

  @Override
  public int hashCode() {
    return RGBToDifferenceChroma.class.hashCode() ^ inverse.hashCode();
  }

  @Override
  public DifferenceChromaToRGB<SO, O, SI> inverse() {
    return inverse;
  }

  @Override
  public SI getInputSpace() {
    return inverse.getOutputSpace();
  }

  @Override
  public SO getOutputSpace() {
    return inverse.getInputSpace();
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    // Y from R, G, and B
    output[0] =
        inverse.kr * input[0] + (1.0 - inverse.kr - inverse.kb) * input[1] + inverse.kb * input[2];
    // Cb from Y and B
    output[1] = inverse.umax * (input[2] - output[0]) / (1.0 - inverse.kb);
    // Cr from Y and R
    output[2] = inverse.vmax * (input[0] - output[0]) / (1.0 - inverse.kr);
    return true;
  }

  @Override
  public String toString() {
    return String.format("RGB -> Yb*r* (kb: %.3f, kr: %.3f, b-max: %.3f, r-max: %.3f)", inverse.kb,
        inverse.kr, inverse.umax, inverse.vmax);
  }
}
