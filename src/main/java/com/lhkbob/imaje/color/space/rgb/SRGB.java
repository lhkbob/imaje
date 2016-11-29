package com.lhkbob.imaje.color.space.rgb;

/**
 * SRGB
 * ====
 *
 * An RGB color space representing the [sRGB standard](https://en.wikipedia.org/wiki/SRGB).
 * Because this space represents a standard with no additional parameters, it has no public
 * constructor and is exposed as a singleton: {@link #INSTANCE}.
 *
 * When in doubt, sRGB is the most likely color space for RGB values.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 2.4, a = 1.0 / 1.055, b = 0.055 / 1.055, c = 0.0, d = 0.04045, e = 1.0
    / 12.92, f = 0.0)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.64, y = 0.33), green = @Chromaticity(x = 0.3, y = 0.6), blue = @Chromaticity(x = 0.15, y = 0.06))
public final class SRGB extends AnnotationRGBSpace<SRGB> {
  /**
   * The singleton instance for the sRGB color space
   */
  public static final SRGB INSTANCE = new SRGB();

  private SRGB() {}
}
