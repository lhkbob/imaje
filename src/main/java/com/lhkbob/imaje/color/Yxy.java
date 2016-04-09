package com.lhkbob.imaje.color;

/**
 *
 */

@Channels({ "Y", "x", "y" })
public class Yxy extends SimpleColor {
  public Yxy() {
    this(0.33333, 0.33333);
  }

  public Yxy(double x, double y) {
    this(1.0, x, y);
  }

  public Yxy(double luminance, double x, double y) {
    super(3);
    set(luminance, x, y);
  }

  @Override
  public Yxy clone() {
    return (Yxy) super.clone();
  }

  public double getLuminance() {
    return channels[0];
  }

  public double getX() {
    return channels[1];
  }

  public double getY() {
    return channels[2];
  }

  public double getZ() {
    return 1.0 - x() - y();
  }

  public double lum() {
    return getLuminance();
  }

  public void lum(double l) {
    setLuminance(l);
  }

  public void setLuminance(double luminance) {
    channels[0] = luminance;
  }

  public void setX(double x) {
    channels[1] = x;
  }

  public void setY(double y) {
    channels[2] = y;
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
