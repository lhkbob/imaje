package com.lhkbob.imaje.color.space.rgb;

/**
 * SMPTEC
 * ======
 *
 * An RGB color space representing the [NTSC's SMPTE C
 * standard](https://en.wikipedia.org/wiki/NTSC#SMPTE_C ). Because this space represents a standard
 * with no additional parameters, it has no public constructor and is exposed as a singleton: {@link
 * #INSTANCE}.
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
  public static final SMPTEC INSTANCE = new SMPTEC();

  private SMPTEC() {}
}
