package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({XYZ.X, XYZ.Y, XYZ.Z})
public class XYZ implements Color {
  public static final String X = "X";
  public static final String Y = "Y";
  public static final String Z = "Z";

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
    switch (channel) {
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
    return x;
  }

  public void x(double x) {
    this.x = x;
  }

  public double y() {
    return y;
  }

  public void y(double y) {
    this.y = y;
  }

  public double z() {
    return z;
  }

  public void z(double z) {
    this.z = z;
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

  @Override
  public int hashCode() {
    int result = Double.hashCode(x);
    result = 31 * result + Double.hashCode(y);
    result = 31 * result + Double.hashCode(z);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    XYZ c = (XYZ) o;
    return Double.compare(c.x, x) == 0 && Double.compare(c.y, y) == 0
        && Double.compare(c.z, z) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), x, y, z);
  }
}
