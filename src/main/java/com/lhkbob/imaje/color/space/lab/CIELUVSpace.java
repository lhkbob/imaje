package com.lhkbob.imaje.color.space.lab;

import com.lhkbob.imaje.color.CIELUV;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Illuminants;

import java.util.Objects;

/**
 * CIELUVSpace
 * ===
 *
 * This color space represents the [1976 CIELUV](https://en.wikipedia.org/wiki/CIELUV) color space
 * that is an attempt at a perceptually uniform color space. This is a parameterized color space
 * that requires a whitepoint in `XYZ<CIE31>` to be specified. Because of this, two instances of
 * `CIELUV` may not be in the same color space based solely on their type.
 *
 * While this color space is similar to {@link CIE} for Lab colors, and was developed at the same
 * time by the ICC, there are no other variants of Luv color spaces. Thus, {@link CIELUV} as a color
 * definition references this space directly. If possible, the {@link CIE} Lab color space is
 * preferred to CIELUV because CIELUV more egregiously fails its perceptual uniformity goals.
 *
 * @author Michael Ludwig
 */
public class CIELUVSpace implements ColorSpace<CIELUV, CIELUVSpace> {
  /**
   * A singleton instance for the CIELUV color space, configured with a D50 standard illuminant
   * and luminance of 1.0 as the whitepoint.
   */
  public static final CIELUVSpace SPACE_D50 = new CIELUVSpace(Illuminants.newD50(1.0).toXYZ());

  private final XYZ<CIE31> referenceWhitepoint;
  private final CIELUVToXYZ toXYZ;

  /**
   * Create a new CIELUV space using the given `referenceWhitepoint`. This whitepoint
   * is required to provide absolute color values to a given set of CIELUV values.
   *
   * @param referenceWhitepoint
   *     The reference whitepoint
   */
  public CIELUVSpace(XYZ<CIE31> referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
    toXYZ = new CIELUVToXYZ(this);
  }

  /**
   * @return The reference whitepoint defining this CIELUV space
   */
  public XYZ<CIE31> getReferenceWhitepoint() {
    return referenceWhitepoint.clone();
  }

  @Override
  public CIELUV newColor() {
    return new CIELUV(this);
  }

  @Override
  public CIELUVToXYZ getXYZTransform() {
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
      return "*u";
    case 2:
      return "*v";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CIELUVSpace)) {
      return false;
    }
    return Objects.equals(((CIELUVSpace) o).referenceWhitepoint, referenceWhitepoint);
  }

  @Override
  public int hashCode() {
    return CIELUVSpace.class.hashCode() ^ referenceWhitepoint.hashCode();
  }

  @Override
  public String toString() {
    return String.format("CIELUV<%s>", referenceWhitepoint);
  }
}
