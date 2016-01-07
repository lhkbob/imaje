package com.lhkbob.imaje.color;

/**
 *
 */
public class XYZ implements Color {
  private double x;
  private double y;
  private double z;

  public XYZ() {
    this(0, 0, 0);
  }

  public XYZ(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  @Override
  public double get(int channel) {
    switch(channel) {
    case 0:
      return x;
    case 1:
      return y;
    case 2:
      return z;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = x;
    array[1] = y;
    array[2] = z;
  }

  @Override
  public void fromArray(double[] array) {
    x = array[0];
    y = array[1];
    z = array[2];
  }

  @Override
  public XYZ clone() {
    try {
      return (XYZ) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen", e);
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

  public void z(double z) {
    setZ(z);
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

  public double getZ() {
    return z;
  }

  public void setZ(double z) {
    this.z = z;
  }
}
