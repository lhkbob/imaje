package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * CIE
 * ===
 *
 * An RGB color space representing [CIE's 1931 RGB
 * standard](https://en.wikipedia.org/wiki/CIE_1931_color_space#CIE_RGB_color_space). Because this
 * space represents a standard with no additional parameters, it has no public constructor and is
 * exposed as a singleton: {@link #SPACE}.
 *
 * The name `CIE` was used instead of `CIERGB` do eliminate redundancy when it is combined with
 * the `RGB` color name: `RGB<CIE>` instead of `RGB<CIERGB>`.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 1.0)
@Illuminant(type = Illuminant.Type.E)
@Primaries(red = @Chromaticity(x = 0.7347, y = 0.2653), green = @Chromaticity(x = 0.2738, y = 0.7174), blue = @Chromaticity(x = 0.1666, y = 0.0089))
public final class CIE extends AnnotationRGBSpace<CIE> {
  /**
   * The singleton instance for the CIE 1931 RGB color space.
   */
  public static final CIE SPACE = new CIE();

  private CIE(){}

  /**
   * @return A new RGB color value in the CIE RGB color space
   */
  public static RGB<CIE> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the CIE RGB color space with given color components. The color
   * components are assumed to be in the CIE RGB color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<CIE> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof CIE;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "CIE '31";
  }
}
