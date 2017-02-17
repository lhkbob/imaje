package com.lhkbob.imaje.color.space.hsv;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.HSV;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.rgb.SRGB;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * HSVSpace
 * ========
 *
 * Represents the hue, saturation, value transformation of a RGB color cube. Further details can
 * be found [here](https://en.wikipedia.org/wiki/HSL_and_HSV#HSV). This space is very similar to the
 * other hue-based {@link HLSSpace}. As a transformation space, HSVSpace creates an intermediate
 * transform from HSV to a particular RGB, which always follows the same logic. The net
 * transformation to XYZ is the concatenation of this intermediate and the RGB space's transform to
 * XYZ.
 *
 * This color space is reasonable intuitive to explore or provide user interfaces for. Unfortunately
 * it is not the most perceptually accurate and should not be used for color comparisons. Instead
 * use one of the {@link com.lhkbob.imaje.color.Lab} spaces or newer color models.
 *
 * @author Michael Ludwig
 */
public class HSVSpace<S extends ColorSpace<RGB<S>, S>> implements ColorSpace<HSV<S>, HSVSpace<S>> {
  /**
   * Singleton representing the HSV transformation of the SRGB color space. Other HSVSpaces can be
   * created as necessary, but given the limited correctness of the HLS system, SRGB is the most
   * likely to be useful.
   */
  public static final HSVSpace<SRGB> SPACE_SRGB = new HSVSpace<>(SRGB.SPACE);

  private final S rgbSpace;
  private final HSVToRGB<S> toRGB;
  private final Transform<HSV<S>, HSVSpace<S>, XYZ<CIE31>, CIE31> toXYZ;

  /**
   * Create a new HSVSpace that is defined in terms of the given `rgbSpace`.
   *
   * @param rgbSpace
   *     The RGB color space to transform
   * @throws NullPointerException
   *     if `rgbSpace` is null
   */
  public HSVSpace(S rgbSpace) {
    Arguments.notNull("rgbSpace", rgbSpace);
    this.rgbSpace = rgbSpace;

    toRGB = new HSVToRGB<>(this);
    toXYZ = new Composition<>(toRGB, rgbSpace.getXYZTransform());
  }

  /**
   * @return The transformed RGB color space.
   */
  public S getRGBSpace() {
    return rgbSpace;
  }

  /**
   * @return The transformation between the HSV color space and thr RGB space.
   */
  public Transform<HSV<S>, HSVSpace<S>, RGB<S>, S> getRGBTransform() {
    return toRGB;
  }

  @Override
  public HSV<S> newColor() {
    return new HSV<>(this);
  }

  @Override
  public Transform<HSV<S>, HSVSpace<S>, XYZ<CIE31>, CIE31> getXYZTransform() {
    return toXYZ;
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  @Override
  public String getChannelName(int channel) {
    switch (channel) {
    case 0:
      return "h";
    case 1:
      return "s";
    case 2:
      return "v";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof HSVSpace)) {
      return false;
    }
    HSVSpace s = (HSVSpace) o;
    return Objects.equals(s.rgbSpace, rgbSpace);
  }

  @Override
  public int hashCode() {
    return HSVSpace.class.hashCode() ^ rgbSpace.hashCode();
  }

  @Override
  public String toString() {
    return String.format("HSV<%s>", rgbSpace);
  }
}
