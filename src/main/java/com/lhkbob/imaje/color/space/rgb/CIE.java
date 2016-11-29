package com.lhkbob.imaje.color.space.rgb;

/**
 * CIE
 * ===
 *
 * An RGB color space representing [CIE's 1931 RGB
 * standard](https://en.wikipedia.org/wiki/CIE_1931_color_space#CIE_RGB_color_space). Because this
 * space represents a standard with no additional parameters, it has no public constructor and is
 * exposed as a singleton: {@link #INSTANCE}.
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
  public static final CIE INSTANCE = new CIE();

  private CIE(){}
}
