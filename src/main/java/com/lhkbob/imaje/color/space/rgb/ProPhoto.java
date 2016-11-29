package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * ProPhoto
 * ========
 *
 * An RGB color space representing [ProPhoto RGB
 * standard](https://en.wikipedia.org/wiki/ProPhoto_RGB_color_space). Because this space represents
 * a standard with no additional parameters, it has no public constructor and is exposed as a
 * singleton: {@link #SPACE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 1.8, d = 0.002, e = 0.062)
@Illuminant(type = Illuminant.Type.D50)
@Primaries(red = @Chromaticity(x = 0.7347, y = 0.2653), green = @Chromaticity(x = 0.1596, y = 0.8404), blue = @Chromaticity(x = 0.0366, y = 0.0001))
public final class ProPhoto extends AnnotationRGBSpace<ProPhoto> {
  /**
   * The singleton instance of the ProPhoto color space.
   */
  public static final ProPhoto SPACE = new ProPhoto();

  private ProPhoto() {}

  /**
   * @return A new RGB color value in the ProPhoto color space
   */
  public static RGB<ProPhoto> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the ProPhoto color space with given color components. The color
   * components are assumed to be in the ProPhoto color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<ProPhoto> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof ProPhoto;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Pro Photo";
  }
}
