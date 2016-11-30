package com.lhkbob.imaje.color.space.lab;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.Lab;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.CIELABToXYZ;
import com.lhkbob.imaje.color.transform.Illuminants;

/**
 * CIE
 * ===
 *
 * This color space represents the [1976
 * CIELAB](https://en.wikipedia.org/wiki/Lab_color_space#CIELAB) color space that is an attempt at a
 * perceptually uniform color space. This is a parameterized color space that requires a whitepoint
 * in `XYZ<CIE31>` to be specified. Because of this, two instances of `Lab<CIE>` may not be in the
 * same color space based solely on their type.
 *
 * The name `CIE` was used instead of `CIELAB` do eliminate redundancy when it is combined with
 * the `Lab` color name: `Lab<CIE>` instead of `Lab<CIELAB>`.
 *
 * @author Michael Ludwig
 */
public class CIE implements ColorSpace<Lab<CIE>, CIE> {
  /**
   * A singleton instance using a D50 whitepoint with luminance equal to 1.0; D50 is the
   * whitepoint frequently assumed or used in other software systems for CIE Lab color spaces.
   */
  public static final CIE SPACE_D50 = new CIE(Illuminants.newD50(1.0).toXYZ());

  private final XYZ<CIE31> referenceWhitepoint;
  private final CIELABToXYZ toXYZ;

  /**
   * Create a new CIE Lab space using the given `referenceWhitepoint`. This whitepoint
   * is required to provide absolute color values to a given set of Lab values.
   * @param referenceWhitepoint The reference whitepoint
   */
  public CIE(XYZ<CIE31> referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
    toXYZ = new CIELABToXYZ(this);
  }

  /**
   * @return The reference whitepoint defining this CIELAB space
   */
  public XYZ<CIE31> getReferenceWhitepoint() {
    return referenceWhitepoint.clone();
  }

  @Override
  public Lab<CIE> newColor() {
    return new Lab<>(this);
  }

  @Override
  public CIELABToXYZ getXYZTransform() {
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
      return "L";
    case 1:
      return "*a";
    case 2:
      return "*b";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CIE)) {
      return false;
    }
    return ((CIE) o).referenceWhitepoint.equals(referenceWhitepoint);
  }

  @Override
  public int hashCode() {
    return CIE.class.hashCode() ^ referenceWhitepoint.hashCode();
  }

  @Override
  public String toString() {
    return String.format("CIELAB<%s>", referenceWhitepoint);
  }
}
