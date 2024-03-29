package com.lhkbob.imaje.color.space.xyz;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * YxySpace
 * ========
 *
 * YxySpace represents a transformed space derived against an {@link XYZ} color space. Color values
 * are represented by {@link Yxy}, which includes the luminance (`Y`), and two chromaticities (`x`
 * and `y`). The transformation between `Yxy` and `XYZ` is lossless and
 * [straightforward](https://en.wikipedia.org/wiki/CIE_1931_color_space#CIE_xy_chromaticity_diagram_and_the_CIE_xyY_color_space)
 *
 * @author Michael Ludwig
 */
public class YxySpace<S extends ColorSpace<XYZ<S>, S>> implements ColorSpace<Yxy<S>, YxySpace<S>> {
  /**
   * A singleton YxySpace corresponding to {@link CIE31#SPACE}.
   */
  public static final YxySpace<CIE31> SPACE_CIE31 = new YxySpace<>(CIE31.SPACE);
  /**
   * A singleton YxySpace corresponding to {@link CIE64#SPACE}.
   */
  public static final YxySpace<CIE64> SPACE_CIE64 = new YxySpace<>(CIE64.SPACE);

  private final S xyzSpace;

  private final YxyToXYZ<S> toXYZ;
  private final Transform<Yxy<S>, YxySpace<S>, XYZ<CIE31>, CIE31> toXYZ31;

  /**
   * Create a new YxySpace defined against the given XYZ color space.
   *
   * @param space
   *     The XYZ space underlying this space
   * @throws NullPointerException
   *     if `space` is null
   */
  @SuppressWarnings("unchecked")
  public YxySpace(S space) {
    Arguments.notNull("space", space);
    xyzSpace = space;
    toXYZ = new YxyToXYZ<>(this);

    if (space == CIE31.SPACE) {
      // toXYZ31 is the same as toXYZ, generics just aren't good at sorting that out
      toXYZ31 = (Transform) toXYZ;
    } else {
      // Compose toXYZ with the non-CIE31 XYZ space's transform to CIE31
      toXYZ31 = new Composition<>(toXYZ, space.getTransformToXYZ());
    }
  }

  /**
   * Get the color transformation between a `Yxy` and the XYZ space, `S`, that this space is defined
   * against. If `S` is equal to {@link CIE31#SPACE} then the returned transform is equal to {@link
   * #getTransformToXYZ()}.
   *
   * @return The transform between a Yxy representation and the `S` color space
   */
  public YxyToXYZ<S> getDirectTransformToXYZ() {
    return toXYZ;
  }

  /**
   * Get the color transformation between a `XYZ` in `S`, that this Yxy space is defined against, to
   * this  Yxy space. If `S` is equal to {@link CIE31#SPACE} then the returned transform is
   * equal to {@link #getTransformFromXYZ()} ()}.
   *
   * @return The transform between a RGB representation and the `T` color space
   */
  public XYZToYxy<S> getDirectTransformFromXYZ() {
    return toXYZ.inverse().orElseThrow(UnsupportedOperationException::new);
  }

  /**
   * @return The XYZ color space that this space is defined against
   */
  public S getXYZSpace() {
    return xyzSpace;
  }

  @Override
  public Yxy<S> newColor() {
    return new Yxy<>(this);
  }

  @Override
  public Transform<Yxy<S>, YxySpace<S>, XYZ<CIE31>, CIE31> getTransformToXYZ() {
    return toXYZ31;
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
      return "x";
    case 2:
      return "y";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public int hashCode() {
    return YxySpace.class.hashCode() ^ xyzSpace.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof YxySpace)) {
      return false;
    }

    return Objects.equals(((YxySpace) o).xyzSpace, xyzSpace);
  }

  @Override
  public String toString() {
    return String.format("Yxy (%s)", xyzSpace);
  }
}
