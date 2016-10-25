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

/**
 *
 */
public class RGBToHLS extends AbstractRGBToHueTransform {
  @Override
  public boolean equals(Object o) {
    return o instanceof RGBToHLS;
  }

  @Override
  public RGBToHLS getLocallySafeInstance() {
    // This reuses the output array as a work array (unlike its partner HLSToRGB, which requires a
    // new instance to be locally safe)
    return this;
  }

  @Override
  public int hashCode() {
    return RGBToHLS.class.hashCode();
  }

  @Override
  public HLSToRGB inverted() {
    return new HLSToRGB();
  }

  @Override
  public String toString() {
    return "RGB -> HLS Transform";
  }

  @Override
  public void transform(double[] input, double[] output) {
    // Super class stores hue, and min, max components into output
    super.transform(input, output);

    double hue = output[0];
    double c = output[2] - output[1];
    double saturation;
    double lightness = 0.5 * (output[2] + output[1]);
    if (c < EPS) {
      // Neutral color, use hue = 0 arbitrarily
      hue = 0.0;
      saturation = 0.0;
    } else {
      hue *= 60.0; // Scale hue to 0 to 360 degrees
      saturation = c / (1.0 - Math.abs(2.0 * lightness - 1.0));
    }

    output[0] = hue;
    output[1] = lightness;
    output[2] = saturation;
  }
  private static final double EPS = 1e-8;
}
