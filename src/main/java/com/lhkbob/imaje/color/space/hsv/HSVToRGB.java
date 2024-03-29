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
package com.lhkbob.imaje.color.space.hsv;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.HSV;
import com.lhkbob.imaje.color.RGB;

import java.util.Optional;

/**
 * HSVToRGB
 * ========
 *
 * Color transformation from {@link HSV} to {@link RGB}.
 *
 * @author Michael Ludwig
 */
public class HSVToRGB<S extends ColorSpace<RGB<S>, S>> extends AbstractHueToRGBTransform<HSVSpace<S>, HSV<S>, S> {
  private final RGBToHSV<S> inverse;

  /**
   * Create a new transformation that works with the given HSVSpace.
   *
   * @param inputSpace
   *     The color space for this transformation
   */
  public HSVToRGB(HSVSpace<S> inputSpace) {
    super(inputSpace, inputSpace.getRGBSpace());
    inverse = new RGBToHSV<>(this);
  }

  HSVToRGB(RGBToHSV<S> inverse) {
    super(inverse.getOutputSpace(), inverse.getInputSpace());
    this.inverse = inverse;
  }

  @Override
  public Optional<RGBToHSV<S>> inverse() {
    return Optional.of(inverse);
  }

  @Override
  public String toString() {
    return "HSV -> RGB Transform";
  }

  @Override
  protected void toHueChromaM(double[] input, double[] hcm) {
    hcm[0] = input[0]; // hue
    hcm[1] = input[2] * input[1]; // chroma
    hcm[2] = input[2] - hcm[1]; // m
  }
}
