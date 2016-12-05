package com.lhkbob.imaje.color.space.lab;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.Lab;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Illuminants;

import java.util.Objects;

/**
 * Hunter
 * ======
 *
 * This color space represents the [Hunter
 * Lab](https://en.wikipedia.org/wiki/Lab_color_space#Hunter_Lab) color space that was the first
 * "Lab" percptually-uniform space. This is a parameterized color space that requires a whitepoint
 * in `XYZ<CIE31>` to be specified. Because of this, two instances of `Lab<Hunter>` may not be in
 * the same color space based solely on their type.
 *
 * @author Michael Ludwig
 */
public class Hunter implements ColorSpace<Lab<Hunter>, Hunter> {
  /**
   * A singleton instance of the Hunter space configured to use the C standard illuminant as its
   * whitepoint, with a luminance of 1.0. The C illuminant was the original whitepoint for the
   * Hunter Lab space although new Hunter spaces can still be defined with {@link #Hunter(XYZ)}.
   */
  public static final Hunter SPACE_C = new Hunter(Illuminants.newC(1.0).toXYZ());

  private final XYZ<CIE31> referenceWhitepoint;
  private final HunterLabToXYZ toXYZ;

  /**
   * Create a new Hunter Lab space using the given `referenceWhitepoint`. This whitepoint
   * is required to provide absolute color values to a given set of Lab values.
   *
   * @param referenceWhitepoint
   *     The reference whitepoint
   */
  public Hunter(XYZ<CIE31> referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint.clone();
    toXYZ = new HunterLabToXYZ(this);
  }

  /**
   * @return The reference whitepoint defining this Hunter Lab space
   */
  public XYZ<CIE31> getReferenceWhitepoint() {
    return referenceWhitepoint.clone();
  }

  @Override
  public Lab<Hunter> newColor() {
    return new Lab<>(this);
  }

  @Override
  public HunterLabToXYZ getXYZTransform() {
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
      return "a";
    case 2:
      return "b";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Hunter)) {
      return false;
    }
    return Objects.equals(((Hunter) o).referenceWhitepoint, referenceWhitepoint);
  }

  @Override
  public int hashCode() {
    return Hunter.class.hashCode() ^ referenceWhitepoint.hashCode();
  }

  @Override
  public String toString() {
    return String.format("Hunter Lab<%s>", referenceWhitepoint);
  }
}
