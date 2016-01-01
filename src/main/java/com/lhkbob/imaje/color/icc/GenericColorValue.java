package com.lhkbob.imaje.color.icc;

import java.util.Arrays;

/**
 *
 */
public class GenericColorValue {
  public enum ColorType {
    CIEXYZ,
    NORMALIZED_CIEXYZ,
    PCSXYZ,
    CIELAB,
    PCSLAB,
    GENERIC
  }

  private final ColorType type;
  private final double[] values;

  public GenericColorValue(ColorType type, double[] values) {
    this(type, values, false);
  }

  private GenericColorValue(ColorType type, double[] values, boolean owned) {
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

  public static GenericColorValue cieLABtoPCSLAB(
      GenericColorValue cielab, ColorMatrix chromaticAdaptation) {

  }

  public static GenericColorValue cieXYZ(double x, double y, double z) {
    return new GenericColorValue(ColorType.CIEXYZ, new double[] { x, y, z }, true);
  }

  public static GenericColorValue denormalizeCIEXYZ(
      GenericColorValue nciexyz, double whitepointLuminance) {
    if (nciexyz.type != ColorType.NORMALIZED_CIEXYZ) {
      throw new IllegalArgumentException(
          "Color must be of type NORMALIZED_CIEXYZ, not: " + nciexyz.type);
    }
    // This copies the ciexyz values array into a new array that can then be modified
    GenericColorValue ciexyz = new GenericColorValue(ColorType.CIEXYZ, nciexyz.values);
    for (int i = 0; i < ciexyz.values.length; i++) {
      ciexyz.values[i] *= whitepointLuminance;
    }
    return ciexyz;
  }

  public static GenericColorValue genericColor(double... channelValues) {
    return new GenericColorValue(ColorType.GENERIC, channelValues, true);
  }

  public static GenericColorValue nCIEXYZ(double x, double y, double z) {
    return new GenericColorValue(ColorType.NORMALIZED_CIEXYZ, new double[] { x, y, z }, true);
  }

  public static GenericColorValue normalizeCIEXYZ(
      GenericColorValue ciexyz, double whitepointLuminance) {
    if (ciexyz.type != ColorType.CIEXYZ) {
      throw new IllegalArgumentException("Color must be of type CIEXYZ, not: " + ciexyz.type);
    }
    // This copies the ciexyz values array into a new array that can then be modified
    GenericColorValue nciexyz = new GenericColorValue(ColorType.NORMALIZED_CIEXYZ, ciexyz.values);
    for (int i = 0; i < nciexyz.values.length; i++) {
      nciexyz.values[i] /= whitepointLuminance;
    }
    return nciexyz;
  }

  public static GenericColorValue normalizedCIEXYZtoPCSXYZ(
      GenericColorValue nciexyz, ColorMatrix chromaticAdaptation) {

  }

  public static GenericColorValue pcsLAB(double l, double a, double b) {
    return new GenericColorValue(ColorType.PCSLAB, new double[] { l, a, b }, true);
  }

  public static GenericColorValue pcsLABToNormalizedCIEXYZ(GenericColorValue lab) {

  }

  public static GenericColorValue pcsLABtoCIELAB(
      GenericColorValue pcslab, ColorMatrix invChromaticAdaptation) {

  }

  public static GenericColorValue pcsXYZ(double x, double y, double z) {
    return new GenericColorValue(ColorType.PCSXYZ, new double[] { x, y, z }, true);
  }

  public static GenericColorValue pcsXYZToCIELAB(GenericColorValue xyz) {

  }

  public static GenericColorValue pcsXYZtoNormalizedCIEXYZ(
      GenericColorValue pcsxyz, ColorMatrix invChromaticAdaptation) {

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
