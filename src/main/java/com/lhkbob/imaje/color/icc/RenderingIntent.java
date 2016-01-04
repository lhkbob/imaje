package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum RenderingIntent {
  /**
   * This transformation corresponds to color reproduction on a hypothetical
   * reference reflective medium. Thus, the PCS values represent the appearance
   * of the reproduction as viewed in the reference viewing environment by an
   * observer adapted to that environment.
   *
   * This ensures reasonable consistency across devices but exact colorimetry
   * is not required.
   */
  PERCEPTUAL,
  /**
   * Transformations with this intent will re-scale in-gamut tristimulus values so that
   * the white point of the actual medium maps to the PCS adopted white point. The
   * {@link com.lhkbob.imaje.color.icc.reader.Tag#CHROMATIC_ADAPTATION} defines a
   * chromatic adaptation matrix specifying how to adapt tristimulus values to the
   * adopted white.
   */
  MEDIA_RELATIVE_COLORIMETRIC,
  /**
   * Vendor specific transformations that compromise accuracy to preserve hue or
   * enhance vividness of colors.
   */
  SATURATION,
  /**
   * This transformation does not modify normalized tristimulus values. The
   * class of D_TO_Bx and B_TO_Dx tags define absolute transformations that operate
   * on absolute colorimetric data. If these are not present, the absolute colorimetric
   * values can be calculated from a media relative colorimetric transformation and
   * ratio of media white points.
   */
  ICC_ABSOLUTE_COLORIMETRIC
}
