package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * WideGamut
 * =========
 *
 * An RGB color space representing [Adobe's Wide Gamut RGB
 * standard](https://en.wikipedia.org/wiki/Adobe_Wide_Gamut_RGB_color_space). Because this space
 * represents a standard with no additional parameters, it has no public constructor and is exposed
 * as a singleton: {@link #SPACE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 2.19921875)
@Illuminant(type = Illuminant.Type.D50)
@Primaries(red = @Chromaticity(x = 0.7347, y = 0.2653), green = @Chromaticity(x = 0.1152, y = 0.8264), blue = @Chromaticity(x = 0.1566, y = 0.0177))
public final class WideGamut extends AnnotationRGBSpace<WideGamut> {
  /**
   * The singleton instance for the wide gamut color space.
   */
  public static final WideGamut SPACE = new WideGamut();

  private WideGamut() {}

  /**
   * @return A new RGB color value in the Adobe WideGamut color space
   */
  public static RGB<WideGamut> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the Adobe WideGamut color space with given color components. The color
   * components are assumed to be in the Adobe WideGamut color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<WideGamut> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof WideGamut;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Adobe Wide Gamut";
  }
}
