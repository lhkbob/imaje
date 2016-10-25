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
package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = { "Hue", "Lightness", "Saturation" }, shortNames = { "H", "L", "S"})
public class HLS extends Color {

  public HLS() {
    this(0.0, 0.0, 0.0);
  }

  public HLS(double h, double l, double s) {
    set(h, l, s);
  }

  @Override
  public HLS clone() {
    return (HLS) super.clone();
  }

  public double getHue() {
    return get(0);
  }

  public double getLightness() {
    return get(1);
  }

  public double getSaturation() {
    return get(2);
  }

  public double h() {
    return getHue();
  }

  public void h(double h) {
    setHue(h);
  }

  public double l() {
    return getLightness();
  }

  public void l(double l) {
    setLightness(l);
  }

  public double s() {
    return getSaturation();
  }

  public void s(double s) {
    setSaturation(s);
  }

  public void setHue(double hue) {
    set(0, hue);
  }

  public void setLightness(double lightness) {
    set(1, lightness);
  }

  public void setSaturation(double saturation) {
    set(2, saturation);
  }
}
