package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ Luv.L, Luv.U, Luv.V })
public class Luv implements Color {
  public static final String L = "L";
  public static final String U = "u";
  public static final String V = "v";

  private double l;
  private double u;
  private double v;

  public Luv() {
    this(0, 0, 0);
  }

  public Luv(double l, double u, double v) {
    this.l = l;
    this.u = u;
    this.v = v;
  }

  @Override
  public Luv clone() {
    try {
      return (Luv) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    Luv c = (Luv) o;
    return Double.compare(c.l, l) == 0 && Double.compare(c.u, u) == 0
        && Double.compare(c.v, v) == 0;
  }

  @Override
  public void fromArray(double[] array) {
    l = array[0];
    u = array[1];
    v = array[2];
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return l;
    case 1:
      return u;
    case 2:
      return v;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  public double getL() {
    return l;
  }

  public double getU() {
    return u;
  }

  public double getV() {
    return v;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(l);
    result = 31 * result + Double.hashCode(u);
    result = 31 * result + Double.hashCode(v);
    return result;
  }

  public double l() {
    return l;
  }

  public void l(double l) {
    this.l = l;
  }

  public void setL(double l) {
    this.l = l;
  }

  public void setU(double u) {
    this.u = u;
  }

  public void setV(double v) {
    this.v = v;
  }

  @Override
  public void toArray(double[] array) {
    array[0] = l;
    array[1] = u;
    array[2] = v;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), l, u, v);
  }

  public double u() {
    return u;
  }

  public void u(double u) {
    this.u = u;
  }

  public double v() {
    return v;
  }

  public void v(double v) {
    this.v = v;
  }
}
