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
import com.lhkbob.imaje.color.annot.SpectrumRange;

import java.util.concurrent.ConcurrentHashMap;

/**
 */
public abstract class Spectrum extends Color {
  private static final ConcurrentHashMap<Class<? extends Spectrum>, double[]> wavelengthCache = new ConcurrentHashMap<>();

  private transient double lowBound;
  private transient double highBound;

  @Channels(value = {}, unnamedChannelCount = 32)
  public static class Visible32 extends Spectrum {
    @Override
    public Visible32 clone() {
      return (Visible32) super.clone();
    }
  }

  @Channels(value = { }, unnamedChannelCount = 64)
  public static class Visible64 extends Spectrum {
    @Override
    public Visible64 clone() {
      return (Visible64) super.clone();
    }
  }

  @Channels(value = { }, unnamedChannelCount = 128)
  public static class Visible128 extends Spectrum {
    @Override
    public Visible128 clone() {
      return (Visible128) super.clone();
    }
  }

  // https://en.wikipedia.org/wiki/Infrared, ISO 20473 Mid-Infrared / Thermal
  @SpectrumRange(lowWavelength = 3000.0, highWavelength = 50000.0)
  @Channels(value = { }, unnamedChannelCount = 32)
  public static class Infrared32 extends Spectrum {
    @Override
    public Infrared32 clone() {
      return (Infrared32) super.clone();
    }
  }

  @SpectrumRange(lowWavelength = 3000.0, highWavelength = 50000.0)
  @Channels(value = { }, unnamedChannelCount = 64)
  public static class Infrared64 extends Spectrum {
    @Override
    public Infrared64 clone() {
      return (Infrared64) super.clone();
    }
  }

  @SpectrumRange(lowWavelength = 3000.0, highWavelength = 50000.0)
  @Channels(value = { }, unnamedChannelCount = 128)
  public static class Infrared128 extends Spectrum {
    @Override
    public Infrared128 clone() {
      return (Infrared128) super.clone();
    }
  }

  // https://en.wikipedia.org/wiki/Ultraviolet#Subtypes, union of UV A, B, and C
  @SpectrumRange(lowWavelength = 100.0, highWavelength = 400.0)
  @Channels(value = { }, unnamedChannelCount = 32)
  public static class UltraViolet32 extends Spectrum {
    @Override
    public UltraViolet32 clone() {
      return (UltraViolet32) super.clone();
    }
  }

  @SpectrumRange(lowWavelength = 100.0, highWavelength = 400.0)
  @Channels(value = { }, unnamedChannelCount = 64)
  public static class UltraViolet64 extends Spectrum {
    @Override
    public UltraViolet64 clone() {
      return (UltraViolet64) super.clone();
    }
  }

  @SpectrumRange(lowWavelength = 100.0, highWavelength = 400.0)
  @Channels(value = { }, unnamedChannelCount = 128)
  public static class UltraViolet128 extends Spectrum {
    @Override
    public UltraViolet128 clone() {
      return (UltraViolet128) super.clone();
    }
  }

  public Spectrum() {
    double[] range = wavelengthCache.get(getClass());
    if (range == null) {
      // Lookup @SpectrumRange annotation
      SpectrumRange sr = getClass().getAnnotation(SpectrumRange.class);
      if (sr != null) {
        if (sr.highWavelength() <= sr.lowWavelength()) {
          throw new IllegalStateException("High wavelength must be greater than low wavelength: " + getClass());
        }
        if (sr.highWavelength() < 0 || sr.lowWavelength() < 0) {
          throw new IllegalArgumentException("Wavelengths must be positive: " + getClass());
        }
        range = new double[] { sr.lowWavelength(), sr.highWavelength() };
      } else {
        // Use default
        range = new double[] { SpectrumRange.DEFAULT_LOW_WAVELENGTH, SpectrumRange.DEFAULT_HIGH_WAVELENGTH };
      }

      // Cache so we don't have to lookup annotations with every object creation
      wavelengthCache.put(getClass(), range);
    }

    lowBound = range[0];
    highBound = range[1];
  }

  public double getWavelength(int sample) {
    double step = (highBound - lowBound) / (getChannelCount() - 1);
    return sample * step + lowBound;
  }

  public void setAmplitude(double wavelength, double amplitude) {
    if (wavelength < lowBound || wavelength > highBound) {
      // Do not store value
      return;
    }

    double normalized = (getChannelCount() - 1) * (wavelength - lowBound) / (highBound - lowBound);
    // Round to the nearest wavelength sample
    int idx = (int) Math.round(normalized);
    set(idx, amplitude);
  }

  public void amplitude(double wavelength, double amplitude) {
    setAmplitude(wavelength, amplitude);
  }

  public double getAmplitude(double wavelength) {
    // Wavelength outside of spectrum range reports no amplitude
    if (wavelength < lowBound || wavelength > highBound) {
      return 0.0;
    }

    double normalized = (getChannelCount() - 1) * (wavelength - lowBound) / (highBound - lowBound);
    int idx = (int) Math.floor(normalized);
    if (idx == getChannelCount() - 1) {
      // If we're exactly at the end of the array, no need to interpolation
      return get(getChannelCount() - 1);
    } else {
      double alpha = normalized - idx;
      return alpha * get(idx + 1) + (1.0 - alpha) * get(idx);
    }
  }

  public double amplitude(double wavelength) {
    return getAmplitude(wavelength);
  }

  public final double getLowestWavelength() {
    return lowBound;
  }

  public final double getHighestWavelength() {
    return highBound;
  }
}
