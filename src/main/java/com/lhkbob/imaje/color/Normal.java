package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels({"X", "Y", "Z"})
public class Normal extends Color {
  public Normal() {
    this(0, 0, 0);
  }

  public Normal(double x, double y, double z) {
    set(x, y, z);
  }

  @Override
  public Normal clone() {
    return (Normal) super.clone();
  }

  public double getX() {
    return get(0);
  }

  public double getY() {
    return get(1);
  }

  public double getZ() {
    return get(2);
  }

  public void setX(double x) {
    set(0, x);
  }

  public void setY(double y) {
    set(1, y);
  }

  public void setZ(double z) {
    set(2, z);
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
