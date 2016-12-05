package com.lhkbob.imaje.color.space.luminance;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Illuminants;

import java.util.Objects;

/**
 * Linear
 * ======
 *
 * A Luminance color space that is defined with respect to a white point illuminant. Like the RGB
 * {@link com.lhkbob.imaje.color.space.rgb.Linear} color space, the luminance channel value is in a
 * linear coordinate system. This means that values in the linear luminance space can be considered
 * proper photometric quantities. As a parameterized color space, multiple instances of
 * `Luminance<Linear>` may in fact refer to separate Linear spaces.
 *
 * @author Michael Ludwig
 */
public class Linear implements ColorSpace<Luminance<Linear>, Linear> {
  /**
   * Singleton instance for linear luminance relative to a D65 standard illuminant with luminance of
   * 1.0.
   */
  public static final Linear SPACE_D65 = new Linear(Illuminants.newD65(1.0).toXYZ());

  private final XYZ<CIE31> referenceWhitepoint;
  private final LuminanceToXYZ toXYZ;

  /**
   * Create a new Linear luminance space that is relative to the given `referenceWhitepoint`.
   *
   * @param referenceWhitepoint
   *     The reference whitepoint
   * @throws NullPointerException
   *     if `referenceWhitepoint` is null
   */
  public Linear(XYZ<CIE31> referenceWhitepoint) {
    this.referenceWhitepoint = referenceWhitepoint;
    toXYZ = new LuminanceToXYZ(this);
  }

  /**
   * @return The reference whitepoint that this luminance is relative to
   */
  public XYZ<CIE31> getReferenceWhitepoint() {
    return referenceWhitepoint.clone();
  }

  @Override
  public Luminance<Linear> newColor() {
    return new Luminance<>(this);
  }

  @Override
  public LuminanceToXYZ getXYZTransform() {
    return toXYZ;
  }

  @Override
  public int getChannelCount() {
    return 1;
  }

  @Override
  public String getChannelName(int channel) {
    if (channel == 0) {
      return "Y";
    } else {
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Linear)) {
      return false;
    }
    return Objects.equals(((Linear) o).referenceWhitepoint, referenceWhitepoint);
  }

  @Override
  public int hashCode() {
    return Linear.class.hashCode() ^ referenceWhitepoint.hashCode();
  }

  @Override
  public String toString() {
    return String.format("Linear<Luminance<%s>>", referenceWhitepoint);
  }
}
