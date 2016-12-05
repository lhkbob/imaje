package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.ColorTransform;

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
   * color instances of type C and produces XYZ values. The transform, if possible, also have a
   * non-null inverse so that XYZ CIE 31 values can be converted into this space.
   *
   * This transformation is the primary means by which the color space is defined relative to a
   * known standard.
   *
   * @return The color transform from this space to XYZ
   */
  ColorTransform<S, C, CIE31, XYZ<CIE31>> getXYZTransform();

  /**
   * Get the human-readable name associated with the channel index, such as 'red', 'green', etc.
   *
   * @param channel
   *     The channel index
   * @return The name of the channel
   */
  String getChannelName(int channel);
}
