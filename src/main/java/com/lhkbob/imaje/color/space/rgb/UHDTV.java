package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * UHDTV
 * =====
 *
 * An RGB color space representing the [UHDTV (ITU-R BT.2020)
 * standard](https://en.wikipedia.org/wiki/Rec._2020). Because this space represents a standard with
 * no additional parameters, it has no public constructor and is exposed as a singleton: {@link
 * #SPACE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 2.222, a = 0.91, b = 0.09, c = 0.0, d = 0.018, e = 0.222, f = 0.0)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.708, y = 0.292), green = @Chromaticity(x = 0.170, y = 0.797), blue = @Chromaticity(x = 0.131, y = 0.046))
public final class UHDTV extends AnnotationRGBSpace<UHDTV> {
  /**
   * The singleton instance for the UHDTV color space.
   */
  public static final UHDTV SPACE = new UHDTV();

  private UHDTV() {}

  /**
   * @return A new RGB color value in the UHDTV color space
   */
  public static RGB<UHDTV> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the UHDTV color space with given color components. The color
   * components are assumed to be in the UHDTV color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<UHDTV> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof UHDTV;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "UHDTV BT.2020";
  }
}
