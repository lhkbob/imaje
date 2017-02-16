package com.lhkbob.imaje.color.space.xyz;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.util.Arguments;

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
  private final ColorTransform<YxySpace<S>, Yxy<S>, CIE31, XYZ<CIE31>> toXYZ31;

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
      toXYZ31 = (ColorTransform) toXYZ;
    } else {
      // Compose toXYZ with the non-CIE31 XYZ space's transform to CIE31
      toXYZ31 = new Composition<>(toXYZ, space.getXYZTransform());
    }
  }

  /**
   * Get the color transformation between a `Yxy` and the XYZ space, `S`, that this space is defined
   * against. If `S` is equal to {@link CIE31#SPACE} then the returned transform is equal to {@link
   * #getXYZTransform()}.
   *
   * @return The transform between a Yxy representation and the `S` color space
   */
  public YxyToXYZ<S> getDirectXYZTransform() {
    return toXYZ;
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
  public ColorTransform<YxySpace<S>, Yxy<S>, CIE31, XYZ<CIE31>> getXYZTransform() {
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
}
