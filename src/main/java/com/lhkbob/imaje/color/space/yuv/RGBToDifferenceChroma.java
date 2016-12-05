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
package com.lhkbob.imaje.color.space.yuv;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.YCbCr;
import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * RGBToDifferenceChroma
 * =====================
 *
 * Color transformation for difference chroma models like {@link YUV} or {@link YCbCr} to {@link
 * RGB}.
 *
 * @author Michael Ludwig
 */
public class RGBToDifferenceChroma<SI extends ColorSpace<RGB<SI>, SI>, SO extends ColorSpace<O, SO>, O extends Color<O, SO>> implements ColorTransform<SI, RGB<SI>, SO, O> {
  private final SO yCbCrSpace;
  private final SI rgbSpace;

  private final double kb;
  private final double kr;
  private final double umax;
  private final double vmax;

  private final DifferenceChromaToRGB<SO, O, SI> inverse;

  /**
   * Create a transformation from RGB to a difference chroma color space that is defined by
   * the given difference weights `kb` and `kr` (for the blue and red channels respectively).
   * `umax` and `vmax` determine the absolute value for the maximum values of the
   * difference chroma channels.
   *
   * @param rgbSpace
   *     The RGB space
   * @param yCbCrSpace
   *     The difference chroma space
   * @param kb
   *     The weight of the blue channel
   * @param kr
   *     The weight of the red channel
   * @param umax
   *     The max of the first difference chroma value
   * @param vmax
   *     The ma of the second difference chroma value
   */
  public RGBToDifferenceChroma(
      SI rgbSpace, SO yCbCrSpace, double kb, double kr, double umax, double vmax) {
    Arguments.notNull("yCbCrSpace", yCbCrSpace);
    Arguments.notNull("rgbSpace", rgbSpace);

    this.yCbCrSpace = yCbCrSpace;
    this.rgbSpace = rgbSpace;

    this.kr = kr;
    this.kb = kb;
    this.umax = umax;
    this.vmax = vmax;

    inverse = new DifferenceChromaToRGB<>(this, kb, kr, umax, vmax);
  }

  RGBToDifferenceChroma(
      DifferenceChromaToRGB<SO, O, SI> inverse, double kb, double kr, double umax, double vmax) {
    yCbCrSpace = inverse.getInputSpace();
    rgbSpace = inverse.getOutputSpace();

    this.kr = kr;
    this.kb = kb;
    this.umax = umax;
    this.vmax = vmax;

    this.inverse = inverse;
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
    if (!Objects.equals(c.yCbCrSpace, yCbCrSpace) || !Objects.equals(c.rgbSpace, rgbSpace)) {
      return false;
    }
    return Double.compare(c.kr, kr) == 0 && Double.compare(c.kb, kb) == 0
        && Double.compare(c.umax, umax) == 0 && Double.compare(c.vmax, vmax) == 0;
  }

  @Override
  public int hashCode() {
    int result = RGBToDifferenceChroma.class.hashCode();
    result = 31 * result + Double.hashCode(kr);
    result = 31 * result + Double.hashCode(kb);
    result = 31 * result + Double.hashCode(umax);
    result = 31 * result + Double.hashCode(vmax);
    result = 31 * result + yCbCrSpace.hashCode();
    result = 31 * result + rgbSpace.hashCode();
    return result;
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
    output[0] = kr * input[0] + (1.0 - kr - kb) * input[1] + kb * input[2];
    // Cb from Y and B
    output[1] = umax * (input[2] - output[0]) / (1.0 - kb);
    // Cr from Y and R
    output[2] = vmax * (input[0] - output[0]) / (1.0 - kr);
    return true;
  }

  @Override
  public String toString() {
    return String
        .format("RGB -> Yb*r* (kb: %.3f, kr: %.3f, b-max: %.3f, r-max: %.3f)", kb, kr, umax, vmax);
  }
}
