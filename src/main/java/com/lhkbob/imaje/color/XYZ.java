package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "X", "Y", "Z" })
public class XYZ extends SimpleColor {
  public XYZ() {
    this(0, 0, 0);
  }

  public XYZ(double x, double y, double z) {
    super(3);
    set(x, y, z);
  }

  @Override
  public XYZ clone() {
    return (XYZ) super.clone();
  }

  public double getX() {
    return channels[0];
  }

  public double getY() {
    return channels[1];
  }

  public double getZ() {
    return channels[2];
  }

  public void setX(double x) {
    channels[0] = x;
  }

  public void setY(double y) {
    channels[1] = y;
  }

  public void setZ(double z) {
    channels[2] = z;
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

  public void z(double z) {
    setZ(z);
  }
}
