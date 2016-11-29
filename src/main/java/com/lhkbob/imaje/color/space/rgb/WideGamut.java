package com.lhkbob.imaje.color.space.rgb;

/**
 * WideGamut
 * =========
 *
 * An RGB color space representing [Adobe's Wide Gamut RGB
 * standard](https://en.wikipedia.org/wiki/Adobe_Wide_Gamut_RGB_color_space). Because this space
 * represents a standard with no additional parameters, it has no public constructor and is exposed
 * as a singleton: {@link #INSTANCE}.
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
  public static final WideGamut INSTANCE = new WideGamut();

  private WideGamut() {}
}
