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

import com.lhkbob.imaje.color.space.spectrum.SpectrumSpace;
import com.lhkbob.imaje.util.Functions;

/**
 */
public class Spectrum<S extends SpectrumSpace<S>> extends Color<Spectrum<S>, S> {
  public Spectrum(S space) {
    super(space, space.getChannelCount());
  }

  public void setAmplitude(double wavelength, double amplitude) {
    double normalized = getColorSpace().getChannel(wavelength);
    // Do not store values outside of range
    if (normalized < 0) {
      return;
    }

    // Round to the nearest wavelength sample
    int idx = Functions.roundToInt(normalized);
    set(idx, amplitude);
  }

  public void amplitude(double wavelength, double amplitude) {
    setAmplitude(wavelength, amplitude);
  }

  public double getAmplitude(double wavelength) {
    double normalized = getColorSpace().getChannel(wavelength);
    // Wavelength outside of spectrum range reports no amplitude
    if (normalized < 0) {
      return 0.0;
    }

    int idx = Functions.floorInt(normalized);
    if (idx == getChannelCount() - 1) {
      // If we're exactly at the end of the array, no need to interpolate
      return get(getChannelCount() - 1);
    } else {
      double alpha = normalized - idx;
      return alpha * get(idx + 1) + (1.0 - alpha) * get(idx);
    }
  }

  public double amplitude(double wavelength) {
    return getAmplitude(wavelength);
  }
}
