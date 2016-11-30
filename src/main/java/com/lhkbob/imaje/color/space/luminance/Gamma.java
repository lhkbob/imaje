package com.lhkbob.imaje.color.space.luminance;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Illuminants;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.curves.UnitGammaFunction;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.color.transform.CurveTransform;

import java.util.Collections;

/**
 * Gamma
 * =====
 *
 * Gamma represents a Luminance color space that has been gamma encoded into non-linear compressed
 * values. It is parameterized and defined relative to an XYZ whitepoint illuminant and has a
 * function that decodes gamma-encoded values into linear luminance. Because values in this
 * space are not in a linear coordinate system, they do not represent photometric values and
 * must first be converted to a linear space.
 *
 * @author Michael Ludwig
 */
public class Gamma implements ColorSpace<Luminance<Gamma>, Gamma> {
  /**
   * A singleton instance representing luminance values encoded with the same gamma function
   * and illuminant as the {@link com.lhkbob.imaje.color.space.rgb.SRGB} color space.
   */
  public static final Gamma SPACE_SRGB = new Gamma(Illuminants.newD65(1.0).toXYZ(),
      new UnitGammaFunction(2.4, 1.0 / 1.055, 0.055 / 1.055, 0.0, 1.0 / 12.92, 0.0, 0.04045));

  private final Linear baseSpace;
  private final ColorTransform<Gamma, Luminance<Gamma>, Linear, Luminance<Linear>> gammaDecoder;
  private final ColorTransform<Gamma, Luminance<Gamma>, CIE31, XYZ<CIE31>> toXYZ;

  /**
   * Create a new Gamma luminance color space that is defined relative to the given whitepoint,
   * passed to {@link Linear#Linear(XYZ)} and represents values gamma-encoded such that
   * `gammaDecoder` transforms the encoded values into linear luminance.
   *
   * @param referenceWhitepoint
   *     The reference whitepoint
   * @param gammaDecoder
   *     The gamma decoding function
   * @throws NullPointerException
   *     if either argument is null
   */
  public Gamma(XYZ<CIE31> referenceWhitepoint, Curve gammaDecoder) {
    baseSpace = new Linear(referenceWhitepoint);
    this.gammaDecoder = new CurveTransform<>(
        this, baseSpace, Collections.singletonList(gammaDecoder));
    toXYZ = new Composition<>(this.gammaDecoder, baseSpace.getXYZTransform());
  }

  /**
   * @return The underlying linear luminance space this depends on
   */
  public Linear getLinearSpace() {
    return baseSpace;
  }

  /**
   * @return The gamma decoding transformation from non-linear to linear space.
   */
  public ColorTransform<Gamma, Luminance<Gamma>, Linear, Luminance<Linear>> getGammaDecoder() {
    return gammaDecoder;
  }

  @Override
  public Luminance<Gamma> newColor() {
    return new Luminance<>(this);
  }

  @Override
  public ColorTransform<Gamma, Luminance<Gamma>, CIE31, XYZ<CIE31>> getXYZTransform() {
    return toXYZ;
  }

  @Override
  public int getChannelCount() {
    return baseSpace.getChannelCount();
  }

  @Override
  public String getChannelName(int channel) {
    return baseSpace.getChannelName(channel);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Gamma)) {
      return false;
    }
    Gamma g = (Gamma) o;
    return g.baseSpace.equals(baseSpace) && g.gammaDecoder.equals(gammaDecoder);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + baseSpace.hashCode();
    result = 31 * result + gammaDecoder.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("Gamma-encoded Luminance<%s>", baseSpace.getReferenceWhitepoint());
  }
}
