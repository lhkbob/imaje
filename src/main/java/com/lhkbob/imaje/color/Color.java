package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.space.xyz.CIE31;

/**
 * Color
 * =====
 *
 * Color is a subclass of Vector that restricts the vector space to be a {@link ColorSpace}. Besides
 * providing utility functions to convert the color to {@link XYZ} using the transformation provided
 * by its color space, Color is mostly a semantic type that designates that subclasses provide an
 * actual representation of color or spectral luminance, etc.
 *
 * @author Michael Ludwig
 */
public abstract class Color<C extends Color<C, S>, S extends ColorSpace<C, S>> extends Vector<C, S> {
  /**
   * Create a new Color that is associated with the given `space` and required dimensionality. An
   * exception is thrown if the space's channel count does not equal `requiredDimensions`. The
   * `requiredDimensions` parameter should not be exposed by subclasses in a public constructor. It
   * is used to enforce valid vector spaces are used (e.g. RGB requires 3 dimensions regardless of
   * the vector space instance). If the vector subclass does not have a hardcoded requirement, it
   * can just pass in `space.getChannelCount()`.
   *
   * @param space
   *     The color space associated with this color
   * @param requiredDimensions
   *     The required dimensions of the color space
   */
  protected Color(S space, int requiredDimensions) {
    super(space, requiredDimensions);
  }

  /**
   * @return The color space, which is the same value reported by {@link #getVectorSpace()}
   */
  public final S getColorSpace() {
    return getVectorSpace();
  }

  /**
   * Convert this color instance to a new XYZ using the XYZ transformation defined by the
   * color's color space.
   *
   * @return The XYZ equivalent to this color
   */
  @SuppressWarnings("unchecked")
  public XYZ<CIE31> toXYZ() {
    return getColorSpace().getTransformToXYZ().apply((C) this);
  }

  /**
   * Convert this color instance to XYZ, storing the calculation in `result`, using the XYZ
   * transformation defined by the color's color space.
   *
   * @return The gamut response from the XYZ transformation
   */
  @SuppressWarnings("unchecked")
  public boolean toXYZ(XYZ<CIE31> result) {
    return getColorSpace().getTransformToXYZ().apply((C) this, result);
  }

  /**
   * Convert the given `xyz` color into the color space of this instance and store the calculated
   * values into this instance.
   *
   * @param xyz The XYZ color to convert
   * @return The gamut response from the XYZ transformation
   */
  @SuppressWarnings("unchecked")
  public boolean fromXYZ(XYZ<CIE31> xyz) {
    return getColorSpace().getTransformFromXYZ().apply(xyz, (C) this);
  }
}
