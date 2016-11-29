package com.lhkbob.imaje.color.space.rgb;

/**
 * HDTV
 * =====
 *
 * An RGB color space representing the [HDTV (ITU-R BT.709)
 * standard](https://en.wikipedia.org/wiki/Rec._709). Because this space represents a standard with
 * no additional parameters, it has no public constructor and is exposed as a singleton: {@link
 * #INSTANCE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 1.0 / 0.45, a = 1.0 / 1.099, b = 0.099 / 1.099, c = 0.0, d = 0.081, e = 1.0
    / 4.5, f = 0.0)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.64, y = 0.33), green = @Chromaticity(x = 0.3, y = 0.6), blue = @Chromaticity(x = 0.15, y = 0.06))
public final class HDTV extends AnnotationRGBSpace<HDTV> {
  /**
   * The singleton instance of the HDTV color space.
   */
  public static final HDTV INSTANCE = new HDTV();

  private HDTV() {}
}
