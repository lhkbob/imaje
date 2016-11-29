package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * Adobe
 * =====
 *
 * An RGB color space representing [Adobe's RGB 98
 * standard](https://en.wikipedia.org/wiki/Adobe_RGB_color_space). Because this space represents a
 * standard with no additional parameters, it has no public constructor and is exposed as a
 * singleton: {@link #SPACE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 2.19921875)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.64, y = 0.33), green = @Chromaticity(x = 0.21, y = 0.71), blue = @Chromaticity(x = 0.15, y = 0.06))
public final class Adobe extends AnnotationRGBSpace<Adobe> {
  /**
   * The singleton instance of the Adobe color space.
   */
  public static final Adobe SPACE = new Adobe();

  private Adobe() {}

  /**
   * @return A new RGB color value in the Adobe color space
   */
  public static RGB<Adobe> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the Adobe color space with given color components. The color
   * components are assumed to be in the Adobe color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<Adobe> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof Adobe;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Adobe '98";
  }
}
