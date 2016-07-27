package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;

/**
 *
 */
public final class GenericColorValue {
  public enum ColorType {
    /**
     * XYZ tristimulus values where Y is in candelas per square meter.
     */
    CIEXYZ,
    /**
     * XYZ values that have been scaled so that nY = 1.0 for the adopted white point.
     * This is for ICC-absolute colorimetric data (although the term ICC-absolute
     * is actually more appropriately considered as colorimetry relative to the
     * reference adopted white).
     */
    NORMALIZED_CIEXYZ,
    /**
     * Normalized XYZ values that have been linearly transformed or chromatically
     * adapted so that the media white point is `(0.9642, 1.0, 0.8249)` in PCSXYZ space.
     * This is for media-relative colorimetric data.
     */
    PCSXYZ,
    /**
     * CIE1976 L*, a*, b* values calculated from normalized XYZ values.
     */
    CIELAB,
    /**
     * CIE1976 L*, a*, b* values calculated from PCS XYZ values.
     */
    PCSLAB,
    /**
     * Any other color representation, such as RGB, HSV, CMYK. This can also be used to
     * represent unknown colorimetry. Unlike the other color types, which are restricted
     * to 3 channels, this may have any number of channels. Two colors values of type GENERIC
     * do not necessarily come from the same color space.
     */
    GENERIC
  }

  private final ColorType type;
  private final double[] values;

  public GenericColorValue(ColorType type, double[] values) {
    this(type, values, false);
  }

  private GenericColorValue(ColorType type, double[] values, boolean owned) {
    Arguments.notNull("values", values);

    if (type != ColorType.GENERIC && values.length != 3) {
      throw new IllegalArgumentException(
          "Non-generic color types expect 3 channels, not " + values.length);
    }
    this.values = (owned ? values : Arrays.copyOf(values, values.length));
    this.type = type;
  }

  public static GenericColorValue cieLAB(double l, double a, double b) {
    return new GenericColorValue(ColorType.CIELAB, new double[] { l, a, b }, true);
  }

  public static GenericColorValue cieXYZ(double x, double y, double z) {
    return new GenericColorValue(ColorType.CIEXYZ, new double[] { x, y, z }, true);
  }

  public static GenericColorValue genericColor(double... channelValues) {
    return new GenericColorValue(ColorType.GENERIC, channelValues, true);
  }

  public static GenericColorValue nCIEXYZ(double x, double y, double z) {
    return new GenericColorValue(ColorType.NORMALIZED_CIEXYZ, new double[] { x, y, z }, true);
  }

  public static GenericColorValue pcsLAB(double l, double a, double b) {
    return new GenericColorValue(ColorType.PCSLAB, new double[] { l, a, b }, true);
  }

  public static GenericColorValue pcsXYZ(double x, double y, double z) {
    return new GenericColorValue(ColorType.PCSXYZ, new double[] { x, y, z }, true);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof GenericColorValue)) {
      return false;
    }

    GenericColorValue c = (GenericColorValue) o;
    return c.type == type && Arrays.equals(c.values, values);
  }

  public double getChannel(int channel) {
    return values[channel];
  }

  public int getChannelCount() {
    return values.length;
  }

  public ColorType getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return type.hashCode() ^ Arrays.hashCode(values);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Color (type: ").append(type).append(", value: [");
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(String.format("%.4f", values[i]));
    }
    sb.append("])");
    return sb.toString();
  }
}
