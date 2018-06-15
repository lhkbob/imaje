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

import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.space.xyz.CIE31;

/**
 *
 */
// FIXME what package does this belong to?
  // FIXME could we also have tag interfaces, or parametric interfaces that describe the illuminant for a color space
  // e.g. HDTV implements D65, or HDTV implenents WhitePoint<D65>
  // FIXME could we also have a ChromaticAdapt color space that takes another color space
  // and a new white point to adapt it to that
  // FIXME maybe I should have tag interfaces that describe the photometric intent (e.g. scene/viewer relative)
  // I need to review the HDR photography and color space books before I can really make these
  // decisions, since it may be that the white point used for certain spaces like CIE XYZ are
  // not part of the color space, since they are scene relative (e.g. the white point already
  // manipulated the spectra)
  //
  // However, I could maybe make a ViewerRelative space that takes another color space and a white point?
  // - only downside is that you can't inherit generic properties, so it's not actually just the
  // 2 params. It's the color, the color space, and the white point it is adapted to
  // (and if we're going to encode the white point for viewer relative, then do we also want to
  // include what the other end point is? Usually viewer relative spaces have a single white point
  // they are defined against, or they allow the white point to be specified (CIELAB). In either
  // case these illuminants may not be a defined standard, so it gets trickier/less desirable to
  // push them into the type system.

  // Really all I'm hoping for here is a way to distinguish between XYZ normalized, XYZ absolute,
  // and XYZ relative to the ICC PCS space (and do the same for CIELAB and CIELUV). I could define
  // additional color spaces? XYZ normalized would be CIE31; XYZ absolute has a trivial transform
  // and parameter for the white point's luminance. And PCS space would also be CIE31? And then
  // normalized is something else (that has the extra data needed to form the chromatic adaptation
  // transform from its white point to the PCS white point?)

  // We do need a way to track illuminants and intents; perhaps this is just a function pushed into ColorSpace?
public final class Illuminants {
  private Illuminants() {}

  public static Yxy<CIE31> newA(double luminance) {
    return Yxy.newCIE31(luminance, 0.44757, 0.40745);
  }

  public static Yxy<CIE31> newB(double luminance) {
    return Yxy.newCIE31(luminance, 0.34842, 0.35161);
  }

  public static Yxy<CIE31> newC(double luminance) {
    return Yxy.newCIE31(luminance, 0.31006, 0.31616);
  }

  public static Yxy<CIE31> newCorrelatedColorTemperature(double temp, double luminance) {
    // See https://en.wikipedia.org/wiki/Planckian_locus
    if (temp < 1667.0 || temp > 25000.0) {
      throw new IllegalArgumentException("Cannot calculate CCT outside of temp [1667, 25000]");
    }

    double x;
    if (temp <= 4000) {
      x = -0.2661239 * 1e9 / (temp * temp * temp) - 0.2343580 * 1e6 / (temp * temp)
          + 0.8776956 * 1e3 / temp + 0.179910;
    } else {
      x = -3.0258469 * 1e9 / (temp * temp * temp) + 2.1070379 * 1e6 / (temp * temp)
          + 0.2226347 * 1e3 / temp + 0.240390;
    }

    double y;
    if (temp <= 2222) {
      y = -1.1063814 * x * x * x - 1.34811020 * x * x + 2.18555832 * x - 0.20219683;
    } else if (temp <= 4000) {
      y = -0.9549476 * x * x * x - 1.37418593 * x * x + 2.09137015 * x - 0.16748867;
    } else {
      y = 3.0817580 * x * x * x - 5.87338670 * x * x + 3.75112997 * x - 0.37001483;
    }

    return Yxy.newCIE31(luminance, x, y);
  }

  public static Yxy<CIE31> newD50(double luminance) {
    return Yxy.newCIE31(luminance, 0.34567, 0.35850);
  }

  public static Yxy<CIE31> newD55(double luminance) {
    return Yxy.newCIE31(luminance, 0.33242, 0.34743);
  }

  public static Yxy<CIE31> newD65(double luminance) {
    return Yxy.newCIE31(luminance, 0.31271, 0.32902);
  }

  public static Yxy<CIE31> newD75(double luminance) {
    return Yxy.newCIE31(luminance, 0.29902, 0.31485);
  }

  public static Yxy<CIE31> newE(double luminance) {
    return Yxy.newCIE31(luminance, 1.0 / 3.0, 1.0 / 3.0);
  }

  public static Yxy<CIE31> newF1(double luminance) {
    return Yxy.newCIE31(luminance, 0.31310, 0.33727);
  }

  public static Yxy<CIE31> newF10(double luminance) {
    return Yxy.newCIE31(luminance, 0.34609, 0.35986);
  }

  public static Yxy<CIE31> newF11(double luminance) {
    return Yxy.newCIE31(luminance, 0.38052, 0.37713);
  }

  public static Yxy<CIE31> newF12(double luminance) {
    return Yxy.newCIE31(luminance, 0.43695, 0.40441);
  }

  public static Yxy<CIE31> newF2(double luminance) {
    return Yxy.newCIE31(luminance, 0.37208, 0.37529);
  }

  public static Yxy<CIE31> newF3(double luminance) {
    return Yxy.newCIE31(luminance, 0.40910, 0.39430);
  }

  public static Yxy<CIE31> newF4(double luminance) {
    return Yxy.newCIE31(luminance, 0.44018, 0.40329);
  }

  public static Yxy<CIE31> newF5(double luminance) {
    return Yxy.newCIE31(luminance, 0.31379, 0.34531);
  }

  public static Yxy<CIE31> newF6(double luminance) {
    return Yxy.newCIE31(luminance, 0.37790, 0.38835);
  }

  public static Yxy<CIE31> newF7(double luminance) {
    return Yxy.newCIE31(luminance, 0.31292, 0.32933);
  }

  public static Yxy<CIE31> newF8(double luminance) {
    return Yxy.newCIE31(luminance, 0.34588, 0.35875);
  }

  public static Yxy<CIE31> newF9(double luminance) {
    return Yxy.newCIE31(luminance, 0.37417, 0.37281);
  }
}
