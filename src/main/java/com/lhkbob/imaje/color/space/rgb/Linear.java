package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.CurveTransform;
import com.lhkbob.imaje.color.transform.RGBToXYZ;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;

/**
 * Linear
 * ======
 *
 * Linear is a special RGB color space that wraps another defined {@link RGBSpace} and represents
 * the linearization or removal of gamma correction for that color space. The defined RGB spaces
 * have several components, which include primary chromaticities and whitepoint, in addition to any
 * gamma encoding that they perform on linear RGB color values.
 *
 * In many cases, operations should be performed on linear color values, but should not discard the
 * other important and defining characteristics of the color space. Thus, this Linear wrapper
 * preserves the primary chromaticities and whitepoint while removing the gamma encoding. It also
 * allows for fairly readable color type definitions for a given RGB: `RGB<Linear<WideGamut>>` is a
 * linear RGB color value in the WideGamut color space.
 *
 * @author Michael Ludwig
 */
public class Linear<S extends RGBSpace<S>> implements ColorSpace<RGB<Linear<S>>, Linear<S>> {
  /**
   * Singleton for the linear Adobe color space.
   */
  public static final Linear<Adobe> SPACE_ADOBE = new Linear<>(Adobe.SPACE);
  /**
   * Singleton for the linear Apple color space.
   */
  public static final Linear<Apple> SPACE_APPLE = new Linear<>(Apple.SPACE);
  /**
   * Singleton for the linear CIE RGB color space.
   */
  public static final Linear<CIE> SPACE_CIE = new Linear<>(CIE.SPACE);
  /**
   * Singleton for the linear HDTV color space.
   */
  public static final Linear<HDTV> SPACE_HDTV = new Linear<>(HDTV.SPACE);
  /**
   * Singleton for the linear NTSC color space.
   */
  public static final Linear<NTSC> SPACE_NTSC = new Linear<>(NTSC.SPACE);
  /**
   * Singleton for the linear PAL color space.
   */
  public static final Linear<PAL> SPACE_PAL = new Linear<>(PAL.SPACE);
  /**
   * Singleton for the linear ProPhoto color space.
   */
  public static final Linear<ProPhoto> SPACE_PRO_PHOTO = new Linear<>(ProPhoto.SPACE);
  /**
   * Singleton for the linear SMPTEC color space.
   */
  public static final Linear<SMPTEC> SPACE_SMPTEC = new Linear<>(SMPTEC.SPACE);
  /**
   * Singleton for the linear sRGB color space.
   */
  public static final Linear<SRGB> SPACE_SRGB = new Linear<>(SRGB.SPACE);
  /**
   * Singleton for the linear UHDTV color space.
   */
  public static final Linear<UHDTV> SPACE_UHDTV = new Linear<>(UHDTV.SPACE);
  /**
   * Singleton for the linear WideGamut color space.
   */
  public static final Linear<WideGamut> SPACE_WIDE_GAMUT = new Linear<>(WideGamut.SPACE);

  private final S rgbSpace;
  private final ColorTransform<Linear<S>, RGB<Linear<S>>, S, RGB<S>> linearTransform;
  private final RGBToXYZ<Linear<S>, CIE31> xyzTransform;

  /**
   * Create a Linear color space wrapping the given `rgbSpace`.
   *
   * @param rgbSpace
   *     The RGBSpace to linearize
   * @throws NullPointerException
   *     if `rgbSpace` is null
   */
  public Linear(S rgbSpace) {
    Arguments.notNull("rgbSpace", rgbSpace);
    this.rgbSpace = rgbSpace;

    RGBToXYZ<S, CIE31> baseTransform = rgbSpace.getXYZTransform();
    // The gamma curve in baseTransform goes from non-linear to linear, but we want the curve
    // that goes from linear to non-linear for use with linearTransform.
    Curve invGamma = (baseTransform.getGammaCurve() == null ? null
        : baseTransform.getGammaCurve().inverted());

    linearTransform = new CurveTransform<>(
        this, rgbSpace, Arrays.asList(invGamma, invGamma, invGamma));
    xyzTransform = baseTransform.getLinearTransform(this);
  }

  /**
   * @return The color transformation from this linear space back to its wrapped, non-linear RGB
   * space
   */
  public ColorTransform<Linear<S>, RGB<Linear<S>>, S, RGB<S>> getGammaCorrection() {
    return linearTransform;
  }

  @Override
  public RGB<Linear<S>> newColor() {
    return new RGB<>(this);
  }

  @Override
  public RGBToXYZ<Linear<S>, CIE31> getXYZTransform() {
    return xyzTransform;
  }

  @Override
  public int getChannelCount() {
    return rgbSpace.getChannelCount();
  }

  @Override
  public String getChannelName(int channel) {
    return rgbSpace.getChannelName(channel);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Linear)) {
      return false;
    }
    return ((Linear<?>) o).rgbSpace.equals(rgbSpace);
  }

  @Override
  public int hashCode() {
    return Linear.class.hashCode() ^ rgbSpace.hashCode();
  }

  @Override
  public String toString() {
    return String.format("Linear<%s>", rgbSpace);
  }
}
