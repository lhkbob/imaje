package com.lhkbob.imaje.color.space.xyz;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.transform.Identity;

import java.io.IOException;

/**
 * CIE31
 * =====
 *
 * Color space corresponding to the [1931 CIE 2 degree
 * observer](https://en.wikipedia.org/wiki/CIE_1931_color_space). Because this space represents a
 * standard with no additional parameters, it has no public constructor and is exposed as a
 * singleton: {@link #SPACE}.
 *
 * @author Michael Ludwig
 */
public final class CIE31 extends XYZSpace<CIE31> {
  /**
   * The singleton instance that defines the CIE31 XYZ color space.
   */
  public static final CIE31 SPACE;

  static {
    try {
      SPACE = new CIE31();
    } catch (IOException e) {
      throw new UnsupportedOperationException(
          "Unable to load color matching functions for CIE31", e);
    }
  }

  private final Identity<CIE31, XYZ<CIE31>, CIE31, XYZ<CIE31>> identity;

  private CIE31() throws IOException {
    identity = new Identity<>(this, this);
    initializeFromCSV("ciexyz31_1.csv");
  }

  @Override
  public Identity<CIE31, XYZ<CIE31>, CIE31, XYZ<CIE31>> getXYZTransform() {
    return identity;
  }

  /**
   * A new XYZ color value in the CIE31 color space with given tristimulus values. The components
   * are assumed to be in the CIE31 color space.
   *
   * @param x
   *     The x component
   * @param y
   *     The y component
   * @param z
   *     The z component
   * @return A new XYZ color
   */
  public static XYZ<CIE31> newXYZ(double x, double y, double z) {
    return new XYZ<>(SPACE, x, y, z);
  }

  @Override
  public int hashCode() {
    return CIE31.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof CIE31;
  }

  @Override
  public String toString() {
    return "CIE '31 XYZ";
  }
}
