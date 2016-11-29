package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * SRGB
 * ====
 *
 * An RGB color space representing the [sRGB standard](https://en.wikipedia.org/wiki/SRGB).
 * Because this space represents a standard with no additional parameters, it has no public
 * constructor and is exposed as a singleton: {@link #SPACE}.
 *
 * When in doubt, sRGB is the most likely color space for RGB values.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 2.4, a = 1.0 / 1.055, b = 0.055 / 1.055, c = 0.0, d = 0.04045, e = 1.0
    / 12.92, f = 0.0)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.64, y = 0.33), green = @Chromaticity(x = 0.3, y = 0.6), blue = @Chromaticity(x = 0.15, y = 0.06))
public final class SRGB extends AnnotationRGBSpace<SRGB> {
  /**
   * The singleton instance for the sRGB color space
   */
  public static final SRGB SPACE = new SRGB();

  private SRGB() {}

  /**
   * @return A new RGB color value in the sRGB color space
   */
  public static RGB<SRGB> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the sRGB color space with given color components. The color
   * components are assumed to be in the sRGB color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<SRGB> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof SRGB;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "sRGB";
  }
}
