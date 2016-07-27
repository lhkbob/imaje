package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.annot.Illuminant;
import com.lhkbob.imaje.color.Yxy;

/**
 *
 */
public final class Illuminants {
  private Illuminants() {}

  public static Yxy fromIlluminant(Illuminant illum) {
    double l = illum.luminance();
    switch (illum.type()) {
    case A:
      return newA(l);
    case B:
      return newB(l);
    case C:
      return newC(l);
    case D50:
      return newD50(l);
    case D55:
      return newD55(l);
    case D65:
      return newD65(l);
    case D75:
      return newD75(l);
    case E:
      return newE(l);
    case F1:
      return newF1(l);
    case F2:
      return newF2(l);
    case F3:
      return newF3(l);
    case F4:
      return newF4(l);
    case F5:
      return newF5(l);
    case F6:
      return newF6(l);
    case F7:
      return newF7(l);
    case F8:
      return newF8(l);
    case F9:
      return newF9(l);
    case F10:
      return newF10(l);
    case F11:
      return newF11(l);
    case F12:
      return newF12(l);
    case TEMPERATURE:
      return newCorrelatedColorTemperature(illum.temperature(), l);
    case CHROMATICITY:
      return new Yxy(l, illum.chromaticity().x(), illum.chromaticity().y());
    default:
      throw new IllegalArgumentException("Unsupported illuminant type: " + illum.type());
    }
  }

  public static Yxy newA(double luminance) {
    return new Yxy(luminance, 0.44757, 0.40745);
  }

  public static Yxy newB(double luminance) {
    return new Yxy(luminance, 0.34842, 0.35161);
  }

  public static Yxy newC(double luminance) {
    return new Yxy(luminance, 0.31006, 0.31616);
  }

  public static Yxy newCorrelatedColorTemperature(double temp, double luminance) {
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

    return new Yxy(luminance, x, y);
  }

  public static Yxy newD50(double luminance) {
    return new Yxy(luminance, 0.34567, 0.35850);
  }

  public static Yxy newD55(double luminance) {
    return new Yxy(luminance, 0.33242, 0.34743);
  }

  public static Yxy newD65(double luminance) {
    return new Yxy(luminance, 0.31271, 0.32902);
  }

  public static Yxy newD75(double luminance) {
    return new Yxy(luminance, 0.29902, 0.31485);
  }

  public static Yxy newE(double luminance) {
    return new Yxy(luminance, 1.0 / 3.0, 1.0 / 3.0);
  }

  public static Yxy newF1(double luminance) {
    return new Yxy(luminance, 0.31310, 0.33727);
  }

  public static Yxy newF10(double luminance) {
    return new Yxy(luminance, 0.34609, 0.35986);
  }

  public static Yxy newF11(double luminance) {
    return new Yxy(luminance, 0.38052, 0.37713);
  }

  public static Yxy newF12(double luminance) {
    return new Yxy(luminance, 0.43695, 0.40441);
  }

  public static Yxy newF2(double luminance) {
    return new Yxy(luminance, 0.37208, 0.37529);
  }

  public static Yxy newF3(double luminance) {
    return new Yxy(luminance, 0.40910, 0.39430);
  }

  public static Yxy newF4(double luminance) {
    return new Yxy(luminance, 0.44018, 0.40329);
  }

  public static Yxy newF5(double luminance) {
    return new Yxy(luminance, 0.31379, 0.34531);
  }

  public static Yxy newF6(double luminance) {
    return new Yxy(luminance, 0.37790, 0.38835);
  }

  public static Yxy newF7(double luminance) {
    return new Yxy(luminance, 0.31292, 0.32933);
  }

  public static Yxy newF8(double luminance) {
    return new Yxy(luminance, 0.34588, 0.35875);
  }

  public static Yxy newF9(double luminance) {
    return new Yxy(luminance, 0.37417, 0.37281);
  }
}
