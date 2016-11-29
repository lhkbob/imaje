package com.lhkbob.imaje.color.space.yuv;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.color.space.rgb.HDTV;
import com.lhkbob.imaje.color.space.rgb.SMPTEC;
import com.lhkbob.imaje.color.space.rgb.UHDTV;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.color.transform.DifferenceChromaToRGB;
import com.lhkbob.imaje.util.Arguments;

/**
 * YUVSpace
 * ========
 *
 * A YUV color space defined in terms of a particular RGB color space. The YUV color space
 * represents luminance and two chrominance coordinates. More details can be found
 * [here](https://en.wikipedia.org/wiki/YUV). This space is highly similar to the other
 * chroma-difference space, {@link YCbCrSpace}. Note that the {@link YUV} color definition is
 * defined directly in terms of `YUVSpace`.
 *
 * The control parameters, `kb` and `kr` for the YUVSpace are dependent on the underlying RGB space.
 * Unfortunately, there currently isn't code to arbitrarily compute these from a random RGB space.
 * Therefore, it is best to use the predefined YUVSpace instances or very carefully determine the
 * appropriate `kb` and `kr` values for other spaces if the intent is to match a standardized
 * YUV space.
 *
 * Currently there is no support for the "studio swing" or "full swing" integer math that was
 * frequently used for analog YUV processing.
 *
 * @author Michael Ludwig
 */
public class YUVSpace<S extends ColorSpace<RGB<S>, S>> implements ColorSpace<YUV<S>, YUVSpace<S>> {
  /**
   * YUV space defined in terms of the {@link UHDTV} RGB space.
   */
  public static final YUVSpace<UHDTV> SPACE_UHDTV = new YUVSpace<>(UHDTV.SPACE, 0.0593, 0.2627);
  /**
   * YUV space defined in terms of the {@link SMPTEC} RGB space.
   */
  public static final YUVSpace<SMPTEC> SPACE_SMPTEC = new YUVSpace<>(SMPTEC.SPACE, 0.114, 0.299);
  /**
   * YUV space defined in terms of the {@link HDTV} RGB space.
   */
  public static final YUVSpace<HDTV> SPACE_HDTV = new YUVSpace<>(HDTV.SPACE, 0.0722, 0.2126);

  private final S rgbSpace;

  private final DifferenceChromaToRGB<YUVSpace<S>, YUV<S>, S> toRGB;
  private final ColorTransform<YUVSpace<S>, YUV<S>, CIE31, XYZ<CIE31>> toXYZ;

  /**
   * Create a new YUVSpace that is defined in terms of the given `rgbSpace` and weights for
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
  public YUVSpace(S rgbSpace, double kb, double kr) {
    Arguments.notNull("rgbSpace", rgbSpace);

    this.rgbSpace = rgbSpace;
    toRGB = DifferenceChromaToRGB.newYUVToRGB(this, kb, kr);
    toXYZ = new Composition<>(toRGB, rgbSpace.getXYZTransform());
  }

  /**
   * @return The defining RGB space
   */
  public S getRGBSpace() {
    return rgbSpace;
  }

  /**
   * @return A transformation from YUV to RGB.
   */
  public DifferenceChromaToRGB<YUVSpace<S>, YUV<S>, S> getRGBTransform() {
    return toRGB;
  }

  @Override
  public YUV<S> newColor() {
    return new YUV<>(this);
  }

  @Override
  public ColorTransform<YUVSpace<S>, YUV<S>, CIE31, XYZ<CIE31>> getXYZTransform() {
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
      return "U";
    case 2:
      return "V";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof YUVSpace)) {
      return false;
    }

    YUVSpace s = (YUVSpace) o;
    return s.rgbSpace.equals(rgbSpace) && s.toRGB.equals(toRGB);
  }

  @Override
  public int hashCode() {
    return rgbSpace.hashCode() ^ toRGB.hashCode();
  }

  @Override
  public String toString() {
    return String.format("YUV<%s>", rgbSpace);
  }
}
