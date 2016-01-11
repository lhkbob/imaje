package com.lhkbob.imaje.color;

/**
 *
 */
@Gamma(gamma = 2.4, a = 1.0 / 1.055, b = 0.055 / 1.055, c = 0.0, d = 0.04045, e = 1.0 / 12.92, f = 0.0)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.64, y = 0.33),
    green = @Chromaticity(x = 0.3, y = 0.6),
    blue = @Chromaticity(x = 0.15, y = 0.06))
public class SRGB extends RGB {
  public SRGB() {

  }

  public SRGB(double r, double g, double b) {
    super(r, g, b);
  }

  @Override
  public SRGB clone() {
    return (SRGB) super.clone();
  }
}
