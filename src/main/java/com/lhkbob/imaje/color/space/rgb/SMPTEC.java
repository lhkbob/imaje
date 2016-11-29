package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.RGB;

/**
 * SMPTEC
 * ======
 *
 * An RGB color space representing the [NTSC's SMPTE C
 * standard](https://en.wikipedia.org/wiki/NTSC#SMPTE_C ). Because this space represents a standard
 * with no additional parameters, it has no public constructor and is exposed as a singleton: {@link
 * #SPACE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 1.0 / 0.45, a = 1.0 / 1.099, b = 0.099 / 1.099, c = 0.0, d = 0.081, e = 1.0
    / 4.5, f = 0.0)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.63, y = 0.34), green = @Chromaticity(x = 0.31, y = 0.595), blue = @Chromaticity(x = 0.155, y = 0.07))
public final class SMPTEC extends AnnotationRGBSpace<SMPTEC> {
  /**
   * The singleton instance for the SMPTE C color space.
   */
  public static final SMPTEC SPACE = new SMPTEC();

  private SMPTEC() {}

  /**
   * @return A new RGB color value in the SMPTEC color space
   */
  public static RGB<SMPTEC> newRGB() {
    return new RGB<>(SPACE);
  }

  /**
   * A new RGB color value in the SMPTEC color space with given color components. The color
   * components are assumed to be in the SMPTEC color space.
   *
   * @param r
   *     The red component
   * @param g
   *     The green component
   * @param b
   *     The blue component
   * @return A new RGB color
   */
  public static RGB<SMPTEC> newRGB(double r, double g, double b) {
    return new RGB<>(SPACE, r, g, b);
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof SMPTEC;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "SMPTE C";
  }
}
