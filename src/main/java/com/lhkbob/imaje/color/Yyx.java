package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ Yyx.Y, Yyx.X_CHROMATICITY, Yyx.Y_CHROMATICITY })
public class Yyx implements Color {
  public static final String X_CHROMATICITY = "x";
  public static final String Y = "Y";
  public static final String Y_CHROMATICITY = "y";
  private double luminance;
  private double x;
  private double y;

  public Yyx() {
    this(0.33333, 0.33333);
  }

  public Yyx(double x, double y) {
    this(1.0, x, y);
  }

  public Yyx(double luminance, double x, double y) {
    this.x = x;
    this.y = y;
    this.luminance = luminance;
  }

  @Override
  public Yyx clone() {
    try {
      return (Yyx) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public void fromArray(double[] array) {
    luminance = array[0];
    x = array[1];
    y = array[2];
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return luminance;
    case 1:
      return x;
    case 2:
      return y;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  public double getLuminance() {
    return luminance;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return 1.0 - x - y;
  }

  public double lum() {
    return getLuminance();
  }

  public void lum(double l) {
    setLuminance(l);
  }

  public void setLuminance(double luminance) {
    this.luminance = luminance;
  }

  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {
    this.y = y;
  }

  @Override
  public void toArray(double[] array) {
    array[0] = luminance;
    array[1] = x;
    array[2] = y;
  }

  public double x() {
    return getX();
  }

  public void x(double x) {
    setX(x);
  }

  public double y() {
    return getY();
  }

  public void y(double y) {
    setY(y);
  }

  public double z() {
    return getZ();
  }
}
