package com.lhkbob.imaje.color.icc;

import java.util.Arrays;

/**
 *
 */
public final class Colorant {
  public static final Colorant EBU_TECH_3213_E = new Colorant(
      new double[] { 0.640, 0.290, 0.150 }, new double[] { 0.330, 0.600, 0.060 });
  public static final Colorant ITU_R_BT_709_2 = new Colorant(
      new double[] { 0.640, 0.300, 0.150 }, new double[] { 0.330, 0.600, 0.060 });
  public static final Colorant P22 = new Colorant(
      new double[] { 0.625, 0.280, 0.155 }, new double[] { 0.340, 0.605, 0.070 });
  public static final Colorant SMPTE_RP145 = new Colorant(
      new double[] { 0.630, 0.310, 0.155 }, new double[] { 0.340, 0.595, 0.070 });
  private final double[] xs;
  private final double[] ys;

  public Colorant(double[] xs, double[] ys) {
    this.xs = xs;
    this.ys = ys;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Colorant)) {
      return false;
    }

    Colorant c = (Colorant) o;
    return Arrays.equals(c.xs, xs) && Arrays.equals(c.ys, ys);
  }

  public int getChannelCount() {
    return xs.length;
  }

  public String getName() {
    if (ITU_R_BT_709_2.equals(this)) {
      return "ITU_R_BT_709_2";
    } else if (SMPTE_RP145.equals(this)) {
      return "SMPTE_RP145";
    } else if (EBU_TECH_3213_E.equals(this)) {
      return "EBU_TECH_3213_E";
    } else if (P22.equals(this)) {
      return "P22";
    } else {
      return null;
    }
  }

  public double getXChromaticity(int channel) {
    return xs[channel];
  }

  public double getYChromaticity(int channel) {
    return ys[channel];
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(xs) ^ Arrays.hashCode(ys);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Colorant (");

    String name = getName();
    if (name != null) {
      sb.append(name).append(", ");
    }

    for (int i = 0; i < getChannelCount(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append('c').append(i + 1).append(": [").append(String.format("%.4f", getXChromaticity(i)))
          .append(", ").append(String.format("%.4f", getYChromaticity(i))).append("]");
    }
    sb.append(")");
    return sb.toString();
  }
}
