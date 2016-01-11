package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({"Y", "x", "y"})
public class Yyx implements Color {
  private double x;
  private double y;
  private double luminance;

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
  public int getChannelCount() {
    return 3;
  }

  @Override
  public double get(int channel) {
    switch(channel) {
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
  public void toArray(double[] array) {
    array[0] = luminance;
    array[1] = x;
    array[2] = y;
  }

  @Override
  public void fromArray(double[] array) {
    luminance = array[0];
    x = array[1];
    y = array[2];
  }

  @Override
  public Yyx clone() {
    try {
      return (Yyx) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
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

  public double lum() {
    return getLuminance();
  }

  public void lum(double l) {
    setLuminance(l);
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getLuminance() {
    return luminance;
  }

  public void setLuminance(double luminance) {
    this.luminance = luminance;
  }

  public double getZ() {
    return 1.0 - x - y;
  }
}
