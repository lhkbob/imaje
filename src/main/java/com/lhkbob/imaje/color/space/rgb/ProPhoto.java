package com.lhkbob.imaje.color.space.rgb;

/**
 * ProPhoto
 * ========
 *
 * An RGB color space representing [ProPhoto RGB
 * standard](https://en.wikipedia.org/wiki/ProPhoto_RGB_color_space). Because this space represents
 * a standard with no additional parameters, it has no public constructor and is exposed as a
 * singleton: {@link #INSTANCE}.
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
  public static final ProPhoto INSTANCE = new ProPhoto();

  private ProPhoto() {}
}
