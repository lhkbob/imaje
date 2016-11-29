package com.lhkbob.imaje.color.space.rgb;

/**
 * Adobe
 * =====
 *
 * An RGB color space representing [Adobe's RGB 98
 * standard](https://en.wikipedia.org/wiki/Adobe_RGB_color_space). Because this space represents a
 * standard with no additional parameters, it has no public constructor and is exposed as a
 * singleton: {@link #INSTANCE}.
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
  public static final Adobe INSTANCE = new Adobe();

  private Adobe() {}
}
