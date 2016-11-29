package com.lhkbob.imaje.color.space.rgb;

/**
 * UHDTV
 * =====
 *
 * An RGB color space representing the [UHDTV (ITU-R BT.2020)
 * standard](https://en.wikipedia.org/wiki/Rec._2020). Because this space represents a standard with
 * no additional parameters, it has no public constructor and is exposed as a singleton: {@link
 * #INSTANCE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 2.222, a = 0.91, b = 0.09, c = 0.0, d = 0.018, e = 0.222, f = 0.0)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.708, y = 0.292), green = @Chromaticity(x = 0.170, y = 0.797), blue = @Chromaticity(x = 0.131, y = 0.046))
public final class UHDTV extends AnnotationRGBSpace<UHDTV> {
  /**
   * The singleton instance for the UHDTV color space.
   */
  public static final UHDTV INSTANCE = new UHDTV();

  private UHDTV() {}
}
