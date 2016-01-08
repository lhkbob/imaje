package com.lhkbob.imaje.color;

/**
 *
 */
@Illuminant(type = Illuminant.Type.D65)
@Chromaticities({
    @Chromaticity(channel = "Red", x = 0.64, y = 0.33),
    @Chromaticity(channel = "Green", x = 0.3, y = 0.6),
    @Chromaticity(channel = "Blue", x = 0.15, y = 0.06)
})
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
