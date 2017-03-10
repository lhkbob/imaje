package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.CurveTransform;
import com.lhkbob.imaje.color.transform.MatrixTransform;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;

import org.ejml.data.DenseMatrix64F;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

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
public class Linear<S extends RGBSpace<S, CIE31>> implements ColorSpace<RGB<Linear<S>>, Linear<S>> {
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
  private final Transform<RGB<Linear<S>>, Linear<S>, RGB<S>, S> linearTransform;
  private final Transform<RGB<Linear<S>>, Linear<S>, XYZ<CIE31>, CIE31> xyzTransform;

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

    RGBToXYZ<S, CIE31> baseTransform = rgbSpace.getRGBToXYZTransform();
    // The gamma curve in baseTransform goes from non-linear to linear, but we want the curve
    // that goes from linear to non-linear for use with linearTransform.
    Curve invGamma;
    if (baseTransform.getDecodingGammaFunction() == null) {
      invGamma = null;
    } else {
      Optional<Curve> encoder = baseTransform.getDecodingGammaFunction().inverted();
      if (!encoder.isPresent()) {
        throw new IllegalStateException("Gamma curve is not invertable");
      }
      invGamma = encoder.get();
    }

    DenseMatrix64F rgbToXYZ = new DenseMatrix64F(3, 3);
    rgbToXYZ.set(baseTransform.getLinearRGBToXYZ());

    linearTransform = new CurveTransform<>(
        this, rgbSpace, Arrays.asList(invGamma, invGamma, invGamma));
    xyzTransform = new MatrixTransform<>(this, CIE31.SPACE, rgbToXYZ, false);
  }

  /**
   * @return The color transformation from this linear space back to its wrapped, non-linear RGB
   * space
   */
  public Transform<RGB<Linear<S>>, Linear<S>, RGB<S>, S> getGammaEncoder() {
    return linearTransform;
  }

  /**
   * @return The color transform from the non-linear RGB space to the linearized coordinate system
   */
  public Transform<RGB<S>, S, RGB<Linear<S>>, Linear<S>> getGammaDecoder() {
    return linearTransform.inverse();
  }

  @Override
  public RGB<Linear<S>> newColor() {
    return new RGB<>(this);
  }

  @Override
  public Transform<RGB<Linear<S>>, Linear<S>, XYZ<CIE31>, CIE31> getXYZTransform() {
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
    return Objects.equals(((Linear<?>) o).rgbSpace, rgbSpace);
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
