package com.lhkbob.imaje.color.space.rgb;

/**
 * NTSC
 * ====
 *
 * An RGB color space representing the [NTSC's original 1953
 * standard](https://en.wikipedia.org/wiki/NTSC#Colorimetry). Because this space represents a
 * standard with no additional parameters, it has no public constructor and is exposed as a
 * singleton: {@link #INSTANCE}.
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
  public static final NTSC INSTANCE = new NTSC();

  private NTSC() {}
}
