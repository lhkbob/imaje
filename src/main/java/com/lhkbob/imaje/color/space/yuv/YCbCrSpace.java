package com.lhkbob.imaje.color.space.yuv;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.YCbCr;
import com.lhkbob.imaje.color.space.rgb.HDTV;
import com.lhkbob.imaje.color.space.rgb.SMPTEC;
import com.lhkbob.imaje.color.space.rgb.UHDTV;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.util.Arguments;

/**
 * YCbCrSpace
 * ========
 *
 * A YCbCr color space defined in terms of a particular RGB color space. The YCbCrcolor space
 * represents luminance and two difference chroma coordinates. More details can be found
 * [here](https://en.wikipedia.org/wiki/YCbCr). This space is highly similar to the other
 * chroma-difference space, {@link YUVSpace}. Note that the {@link YCbCr} color definition is
 * defined directly against `YCbCrSpace`.
 *
 * The control parameters, `kb` and `kr` for the YCbCrSpace are dependent on the underlying RGB
 * space. Unfortunately, there currently isn't code to arbitrarily compute these from a random RGB
 * space. Therefore, it is best to use the predefined YUVSpace instances or very carefully determine
 * the appropriate `kb` and `kr` values for other spaces if the intent is to match a standardized
 * YCbCr space.
 *
 * @author Michael Ludwig
 */
public class YCbCrSpace<S extends ColorSpace<RGB<S>, S>> implements ColorSpace<YCbCr<S>, YCbCrSpace<S>> {
  /**
   * YCbCr space defined in terms of the {@link UHDTV} RGB space.
   */
  public static final YCbCrSpace<UHDTV> SPACE_UHDTV = new YCbCrSpace<>(UHDTV.SPACE, 0.0593, 0.2627);
  /**
   * YCbCr space defined in terms of the {@link SMPTEC} RGB space.
   */
  public static final YCbCrSpace<SMPTEC> SPACE_SMPTEC = new YCbCrSpace<>(
      SMPTEC.SPACE, 0.114, 0.299);
  /**
   * YCbCr space defined in terms of the {@link HDTV} RGB space.
   */
  public static final YCbCrSpace<HDTV> SPACE_HDTV = new YCbCrSpace<>(HDTV.SPACE, 0.0722, 0.2126);

  private final S rgbSpace;

  private final DifferenceChromaToRGB<YCbCr<S>, YCbCrSpace<S>, S> toRGB;
  private final Transform<YCbCr<S>, YCbCrSpace<S>, XYZ<CIE31>, CIE31> toXYZ;

  /**
   * Create a new YCbCrSpace that is defined in terms of the given `rgbSpace` and weights for
   * blue and red.
   *
   * @param rgbSpace
   *     The defining RGB space
   * @param kb
   *     The blue weight
   * @param kr
   *     The red weight
   * @throws NullPointerException
   *     if `rgbSpace` is null
   */
  public YCbCrSpace(S rgbSpace, double kb, double kr) {
    Arguments.notNull("rgbSpace", rgbSpace);

    this.rgbSpace = rgbSpace;
    toRGB = new DifferenceChromaToRGB<>(this, rgbSpace, kb, kr, 0.5, 0.5);
    toXYZ = new Composition<>(toRGB, rgbSpace.getTransformToXYZ());
  }

  /**
   * @return The defining RGB space
   */
  public S getRGBSpace() {
    return rgbSpace;
  }

  /**
   * @return A transformation from YCbCr to RGB.
   */
  public DifferenceChromaToRGB<YCbCr<S>, YCbCrSpace<S>, S> getTransformToRGB() {
    return toRGB;
  }

  /**
   * @return The transformation from RGB to YCbCr.
   */
  public RGBToDifferenceChroma<S, YCbCr<S>, YCbCrSpace<S>> getTransformFromRGB() {
    return toRGB.inverse().orElseThrow(UnsupportedOperationException::new);
  }

  @Override
  public YCbCr<S> newColor() {
    return new YCbCr<>(this);
  }

  @Override
  public Transform<YCbCr<S>, YCbCrSpace<S>, XYZ<CIE31>, CIE31> getTransformToXYZ() {
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
      return "Y";
    case 1:
      return "Cb";
    case 2:
      return "Cr";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }
}
