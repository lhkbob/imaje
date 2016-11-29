package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * NTSC
 * ====
 *
 * An RGB color space representing the [NTSC's original 1953
 * standard](https://en.wikipedia.org/wiki/NTSC#Colorimetry). Because this space represents a
 * standard with no additional parameters, it has no public constructor and is exposed as a
 * singleton: {@link #SPACE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 1.0 / 0.45, a = 1.0 / 1.099, b = 0.099 / 1.099, c = 0.0, d = 0.081, e = 1.0
    / 4.5, f = 0.0)
@Illuminant(type = Illuminant.Type.C)
@Primaries(red = @Chromaticity(x = 0.67, y = 0.33), green = @Chromaticity(x = 0.21, y = 0.71), blue = @Chromaticity(x = 0.14, y = 0.08))
public final class NTSC extends AnnotationRGBSpace<NTSC> {
  /**
   * The singleton instance of the NTSC color space.
   */
  public static final NTSC SPACE = new NTSC();

  private NTSC() {}

  /**
   * @return A new RGB color value in the NTSC color space
   */
  public static RGB<NTSC> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the NTSC color space with given color components. The color
   * components are assumed to be in the NTSC color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<NTSC> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof NTSC;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "NTSC";
  }
}
