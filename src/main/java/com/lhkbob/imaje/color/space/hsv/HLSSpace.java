package com.lhkbob.imaje.color.space.hsv;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.HLS;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.rgb.SRGB;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * HLSSpace
 * ========
 *
 * Represents the hue, lightness, saturation transformation of a RGB color cube. Further details can
 * be found [here](https://en.wikipedia.org/wiki/HSL_and_HSV#HSL). This space is very similar to the
 * other hue-based {@link HSVSpace}. As a transformation space, HLSSpace creates an intermediate
 * transform from HLS to a particular RGB, which always follows the same logic. The net
 * transformation to XYZ is the concatenation of this intermediate and the RGB space's transform to
 * XYZ.
 *
 * This color space is reasonable intuitive to explore or provide user interfaces for. Unfortunately
 * it is not the most perceptually accurate and should not be used for color comparisons. Instead
 * use one of the {@link com.lhkbob.imaje.color.Lab} spaces or newer color models.
 *
 * @author Michael Ludwig
 */
public class HLSSpace<S extends ColorSpace<RGB<S>, S>> implements ColorSpace<HLS<S>, HLSSpace<S>> {
  /**
   * Singleton representing the HLS transformation of the SRGB color space. Other HLSSpaces can be
   * created as necessary, but given the limited correctness of the HLS system, SRGB is the most
   * likely to be useful.
   */
  public static final HLSSpace<SRGB> SPACE_SRGB = new HLSSpace<>(SRGB.SPACE);

  private final S rgbSpace;
  private final HLSToRGB<S> toRGB;
  private final ColorTransform<HLSSpace<S>, HLS<S>, CIE31, XYZ<CIE31>> toXYZ;

  /**
   * Create a new HLSSpace that is defined in terms of the given `rgbSpace`.
   *
   * @param rgbSpace
   *     The RGB color space to transform
   * @throws NullPointerException
   *     if `rgbSpace` is null
   */
  public HLSSpace(S rgbSpace) {
    Arguments.notNull("rgbSpace", rgbSpace);
    this.rgbSpace = rgbSpace;

    toRGB = new HLSToRGB<>(this);
    toXYZ = new Composition<>(toRGB, rgbSpace.getXYZTransform());
  }

  /**
   * @return The transformed RGB color space.
   */
  public S getRGBSpace() {
    return rgbSpace;
  }

  /**
   * @return The transformation between the HLS color space and thr RGB space.
   */
  public ColorTransform<HLSSpace<S>, HLS<S>, S, RGB<S>> getRGBTransform() {
    return toRGB;
  }

  @Override
  public HLS<S> newColor() {
    return new HLS<>(this);
  }

  @Override
  public ColorTransform<HLSSpace<S>, HLS<S>, CIE31, XYZ<CIE31>> getXYZTransform() {
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
      return "l";
    case 2:
      return "s";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof HLSSpace)) {
      return false;
    }
    HLSSpace s = (HLSSpace) o;
    return Objects.equals(s.rgbSpace, rgbSpace);
  }

  @Override
  public int hashCode() {
    return HLSSpace.class.hashCode() ^ rgbSpace.hashCode();
  }

  @Override
  public String toString() {
    return String.format("HLS<%s>", rgbSpace);
  }
}
