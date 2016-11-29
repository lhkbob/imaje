package com.lhkbob.imaje.color.space.rgb;

/**
 * Apple
 * =====
 *
 * An RGB color space representing [Apple's RGB standard](https://en.wikipedia.org/wiki/Apple_RGB).
 * Because this space represents a standard with no additional parameters, it has no public
 * constructor and is exposed as a singleton: {@link #INSTANCE}.
 *
 * @author Michael Ludwig
 */
@Gamma(gamma = 1.801)
@Illuminant(type = Illuminant.Type.D65)
@Primaries(red = @Chromaticity(x = 0.625, y = 0.34), green = @Chromaticity(x = 0.28, y = 0.595), blue = @Chromaticity(x = 0.155, y = 0.07))
public final class Apple extends AnnotationRGBSpace<Apple> {
  /**
   * The singleton instance of the Apple color space.
   */
  public static final Apple INSTANCE = new Apple();

  private Apple() {}
}
