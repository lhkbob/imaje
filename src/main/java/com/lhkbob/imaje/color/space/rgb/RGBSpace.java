package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;

/**
 * RGBSpace
 * ========
 *
 * An abstract color space for {@link RGB} that defines the transformation from RGB to XYZ based off
 * of a whitepoint, primaries for red, green, and blue, and an optional gamma curve that adds a
 * non-linear transformation. This is modeled by the {@link RGBToXYZ} color transformation.
 *
 * Subclasses *must* call {@link #initialize(XYZ, Yxy, Yxy, Yxy, Curve)} from their constructor.
 *
 * Many standardized RGB color spaces are provided in this package as subclasses of RGBSpace.
 * Descriptions and specifications for these spaces can be found
 * [here](https://en.wikipedia.org/wiki/RGB_color_space)
 *
 * @author Michael Ludwig
 */
public abstract class RGBSpace<S extends RGBSpace<S>> implements ColorSpace<RGB<S>, S> {
  private RGBToXYZ<S, CIE31> toXYZ = null; // final after initialize()

  /**
   * Finish initialization of this space by computing its transformation from RGB to XYZ
   * based on the `whitepoint`, primaries and optional `gammaCurve`. These arguments correspond
   * to the arguments in {@link RGBToXYZ#newRGBToXYZ(ColorSpace, XYZ, Yxy, Yxy, Yxy, Curve)}.
   *
   * @param whitepoint
   *     The whitepoint in CIE31 XYZ
   * @param redPrimary
   *     The red primary (Y is ignored)
   * @param greenPrimary
   *     The green primary (Y is ignored)
   * @param bluePrimary
   *     The blue primary (Y is ignored)
   * @param gammaCurve
   *     Gamma curve converting from non-linear to linear values, or null for no transformation
   */
  @SuppressWarnings("unchecked")
  protected void initialize(
      XYZ<CIE31> whitepoint, Yxy<CIE31> redPrimary, Yxy<CIE31> greenPrimary, Yxy<CIE31> bluePrimary,
      @Arguments.Nullable Curve gammaCurve) {
    if (toXYZ != null) {
      throw new IllegalStateException("initialize() already called");
    }

    toXYZ = RGBToXYZ
        .newRGBToXYZ((S) this, whitepoint, redPrimary, greenPrimary, bluePrimary, gammaCurve);
  }

  @Override
  @SuppressWarnings("unchecked")
  public RGB<S> newColor() {
    return new RGB<>((S) this);
  }

  @Override
  public RGBToXYZ<S, CIE31> getXYZTransform() {
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
      return "red";
    case 1:
      return "green";
    case 2:
      return "blue";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }
}
