package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Transform;

/**
 * ColorSpace
 * ==========
 *
 * ColorSpace is an extension of VectorSpace to add semantics around representing actual color
 * values. Specifically, all ColorSpaces must provide a transformation to the 1931 CIE XYZ color
 * space, represented by {@link CIE31} and {@link XYZ}. Note that these transformations do not
 * necessarily have a computable inverse, so it may not be possible to convert back from XYZ into
 * this space. Implementations should endeavor to provide such inversions if possible, but this
 * cannot be guaranteed when color spaces are dynamically defined from ICC color profiles or similar
 * data files.
 *
 * Other than providing this transformation, a ColorSpace is treated equivalently to a VectorSpace.
 * However, Color subclasses should refer to ColorSpaces and direct Vector subclasses should refer
 * to VectorSpaces and not use a ColorSpace. Type generics should normally protect from this
 * scenario.
 *
 * @author Michael Ludwig
 */
public interface ColorSpace<C extends Color<C, S>, S extends ColorSpace<C, S>> extends VectorSpace<C, S> {
  /**
   * @return A new color instance, via {@link #newColor()}
   */
  @Override
  default C newValue() {
    return newColor();
  }

  /**
   * @return A new color instance with zero'ed channel values and of this color space
   */
  C newColor();

  /**
   * Get the transformation from this color space to the CIE 31 space. The transform operates on
   * color instances of type C and produces XYZ values. The transform must have an inverse,
   * although it is allowed to be a pseudo-inverse that loses information or is clamped to
   * a particular gamut.
   *
   * This transformation is the primary means by which the color space is defined relative to a
   * known standard.
   *
   * @return The color transform from this space to XYZ
   */
  Transform<C, S, XYZ<CIE31>, CIE31> getTransformToXYZ();

  /**
   * Get the transformation from the CIE 31 space to this color space. This should return the
   * inverse reported by {@link #getTransformToXYZ()} and is provided as a convenience that
   * extracts the instance from the Optional returned formally by the Transform. Since the
   * XYZ transformation for ColorSpaces are required to have inverses, this is a safe operation.
   *
   * @return The color transform from XYZ to this space
   */
  default Transform<XYZ<CIE31>, CIE31, C, S> getTransformFromXYZ() {
    return getTransformToXYZ().inverse().orElseThrow(UnsupportedOperationException::new);
  }

  /**
   * Get the human-readable name associated with the channel index, such as 'red', 'green', etc.
   *
   * @param channel
   *     The channel index
   * @return The name of the channel
   */
  String getChannelName(int channel);
}
