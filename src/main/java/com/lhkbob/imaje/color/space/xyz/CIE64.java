package com.lhkbob.imaje.color.space.xyz;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.rgb.CustomRGBSpace;
import com.lhkbob.imaje.color.space.spectrum.SmitsRGBToSpectrum;
import com.lhkbob.imaje.color.space.spectrum.SpectrumSpace;
import com.lhkbob.imaje.color.space.spectrum.SpectrumToXYZ;
import com.lhkbob.imaje.color.space.spectrum.Visible;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.color.transform.ExplicitInverse;
import com.lhkbob.imaje.color.transform.curves.Curve;

import java.io.IOException;

import static com.lhkbob.imaje.color.space.spectrum.SpectrumSpace.readSmitsSpectrumBasis;

/**
 * CIE64
 * =====
 *
 * Color space corresponding to the [1964 CIE XYZ 10 degree
 * observer](https://en.wikipedia.org/wiki/CIE_1931_color_space#CIE_standard_observer). Because this
 * space represents a standard with no additional parameters, it has no public constructor and is
 * exposed as a singleton: {@link #SPACE}.
 *
 * The transformation between CIE64 and CIE31 is implemented as composition of first transforming
 * the CIE64 XYZ values back to a spectrum and then converting the spectrum to CIE31 XYZ values. If
 * unsure, you most likely should use CIE31 instead as that is more widely adopted and used as the
 * default XYZ space.
 *
 * @author Michael Ludwig
 */
public class CIE64 extends XYZSpace<CIE64> {
  /**
   * The singleton instance that defines the CIE64 XYZ color space.
   */
  public static final CIE64 SPACE;

  static {
    try {
      SPACE = new CIE64();
    } catch (IOException e) {
      throw new UnsupportedOperationException(
          "Unable to load color matching functions for CIE64", e);
    }
  }

  private final ExplicitInverse<CIE64, XYZ<CIE64>, CIE31, XYZ<CIE31>> toXYZ31;

  private CIE64() throws IOException {
    initializeFromCSV("ciexyz64_1.csv");

    // Use the primaries of sRGB that were used when generating the Smits curves defined in /resources
    CustomRGBSpace<CIE64> rgb64 = new CustomRGBSpace<>(
        this, 1.0, 1.0, 1.0, 0.64, 0.33, 0.3, 0.6, 0.15, 0.06, null);

    // Read special curves generated for XYZ 64 color matching functions
    Curve white = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie64_white.txt");
    Curve cyan = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie64_cyan.txt");
    Curve magenta = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie64_magenta.txt");
    Curve yellow = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie64_yellow.txt");
    Curve red = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie64_red.txt");
    Curve green = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie64_green.txt");
    Curve blue = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie64_blue.txt");

    SmitsRGBToSpectrum<CustomRGBSpace<CIE64>, Visible> rgb64ToSpec = new SmitsRGBToSpectrum<>(
        rgb64, Visible.SPACE_32, white, cyan, magenta, yellow, red, green, blue);
    ColorTransform<CIE64, XYZ<CIE64>, CIE31, XYZ<CIE31>> to31 = new Composition<>(
        rgb64.getRGBToXYZTransform().inverse(),
        new Composition<>(rgb64ToSpec, Visible.SPACE_32.getXYZTransform()));

    SpectrumToXYZ<Visible, CIE64> specToXYZ64 = new SpectrumToXYZ<>(Visible.SPACE_32, this);
    ColorTransform<CIE31, XYZ<CIE31>, CIE64, XYZ<CIE64>> to64 = new Composition<>(
        Visible.SPACE_32.getXYZTransform().inverse(), specToXYZ64);

    toXYZ31 = new ExplicitInverse<>(to31, to64);
  }

  @Override
  public ColorTransform<CIE64, XYZ<CIE64>, CIE31, XYZ<CIE31>> getXYZTransform() {
    return toXYZ31;
  }

  /**
   * A new XYZ color value in the CIE64 color space with given tristimulus values. The components
   * are assumed to be in the CIE64 color space.
   *
   * @param x
   *     The x component
   * @param y
   *     The y component
   * @param z
   *     The z component
   * @return A new XYZ color
   */
  public static XYZ<CIE64> newXYZ(double x, double y, double z) {
    return new XYZ<>(SPACE, x, y, z);
  }

  @Override
  public int hashCode() {
    return CIE64.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof CIE64;
  }

  @Override
  public String toString() {
    return "CIE '64 XYZ";
  }
}
