package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * Apple
 * =====
 *
 * An RGB color space representing [Apple's RGB standard](https://en.wikipedia.org/wiki/Apple_RGB).
 * Because this space represents a standard with no additional parameters, it has no public
 * constructor and is exposed as a singleton: {@link #SPACE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 1.801)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.625, y = 0.34), green = @Chromaticity(x = 0.28, y = 0.595), blue = @Chromaticity(x = 0.155, y = 0.07))
public final class Apple extends AnnotationRGBSpace<Apple> {
  /**
   * The singleton instance of the Apple color space.
   */
  public static final Apple SPACE = new Apple();

  private Apple() {}

  /**
   * @return A new RGB color value in the Apple color space
   */
  public static RGB<Apple> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the Apple color space with given color components. The color
   * components are assumed to be in the Apple color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<Apple> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof Apple;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Apple";
  }
}
