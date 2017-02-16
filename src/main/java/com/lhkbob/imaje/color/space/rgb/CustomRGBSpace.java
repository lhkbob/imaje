package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.space.xyz.XYZSpace;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * CustomRGBSpace
 * ==============
 *
 * An RGBSpace sub class that exposes all parameters to control the linear and non-linear
 * transformation parts going between its RGB components and a specified XYZ color space. This is
 * provided mostly for programmatic creation of RGB color spaces, and should be used with caution.
 * If the XYZ color space for this RGB space is not CIE31, the color transformation to CIE31
 * mandated by the {@link ColorSpace} interface will most likely expand to a spectral representation
 * and then collapse it again using different color matching functions. This is dependent on the
 * implementation of `S`.
 *
 * @author Michael Ludwig
 */
public class CustomRGBSpace<S extends XYZSpace<S>> extends RGBSpace<CustomRGBSpace<S>, S> {
  /**
   * Create a new RGB color space defined against the XYZ color space of `whitepoint`. This
   * constructor should only be used if `S` is already concretely defined without the need of this
   * RGB space.
   *
   * @param whitepoint
   *     The whitepoint of this RGB space
   * @param red
   *     The red primary of this RGB space
   * @param green
   *     The green primary of this RGB space
   * @param blue
   *     The blue primary of this RGB space
   * @param decodingGamma
   *     An optional decoding gamma function if the color space is not linear
   */
  public CustomRGBSpace(
      XYZ<S> whitepoint, Yxy<S> red, Yxy<S> green, Yxy<S> blue,
      @Arguments.Nullable Curve decodingGamma) {
    this(whitepoint.getColorSpace(), whitepoint.getX(), whitepoint.getY(), whitepoint.getZ(),
        red.getX(), red.getY(), green.getX(), green.getY(), blue.getX(), blue.getY(),
        decodingGamma);
  }

  /**
   * Create a new RGB color space defined against the given `xyzSpace`. The tuple `(wx, wy, wz)`
   * represents the XYZ whitepoint of the RGB space. The pairs `(rx, ry)`, `(gx, gy)`, and `(bx, by)`
   * represent the xy chromaticities of the red, green, and blue primaries of this RGB space.
   * All XYZ and xy values are assumed to be in the provided `xyzSpace`. They are not represented
   * as typed instances of {@link com.lhkbob.imaje.color.XYZ} or {@link com.lhkbob.imaje.color.Yxy}
   * to allow breaking cycles in color space initialization. If that is not required, the other
   * constructor can be more compact and type safe.
   *
   * @param xyzSpace
   *     The XYZ color space for this RGB space
   * @param wx
   *     The x tristimulus value of the color representing white in this space
   * @param wy
   *     The y tristimulus value of the color representing white in this space
   * @param wz
   *     The z tristimulus value of the color representing white in this space
   * @param rx
   *     The x chromaticity value of the red primary for this space
   * @param ry
   *     The y chromaticity value of the red primary for this space
   * @param gx
   *     The x chromaticity value of the green primary for this space
   * @param gy
   *     The y chromaticity value of the green primary for this space
   * @param bx
   *     The x chromaticity value of the blue primary for this space
   * @param by
   *     The y chromaticity value of the blue primary for this space
   * @param decodingGamma
   *     An optional decoding gamma function if the color space is not linear
   */
  public CustomRGBSpace(
      S xyzSpace, double wx, double wy, double wz, double rx, double ry, double gx, double gy,
      double bx, double by, @Arguments.Nullable Curve decodingGamma) {
    initialize(xyzSpace, wx, wy, wz, rx, ry, gx, gy, bx, by, decodingGamma);
  }

  @Override
  public int hashCode() {
    return CustomRGBSpace.class.hashCode() ^ getXYZTransform().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof CustomRGBSpace)) {
      return false;
    }

    return Objects.equals(((CustomRGBSpace) o).getXYZTransform(), getXYZTransform());
  }

  @Override
  public String toString() {
    return "Custom RGB";
  }
}
