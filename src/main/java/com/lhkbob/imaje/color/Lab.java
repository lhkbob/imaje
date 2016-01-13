package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ Lab.L, Lab.A, Lab.B })
public class Lab implements Color {
  public static final String A = "a";
  public static final String B = "b";
  public static final String L = "L";

  public static class CIE extends Lab {
    public CIE() {

    }

    public CIE(double l, double a, double b) {
      super(l, a, b);
    }

    @Override
    public CIE clone() {
      return (CIE) super.clone();
    }
  }

  public static class Hunter extends Lab {
    public Hunter() {

    }

    public Hunter(double l, double a, double b) {
      super(l, a, b);
    }

    @Override
    public Hunter clone() {
      return (Hunter) super.clone();
    }
  }
  private double a;
  private double b;
  private double l;

  public Lab() {
    this(0, 0, 0);
  }

  public Lab(double l, double a, double b) {
    this.l = l;
    this.a = a;
    this.b = b;
  }

  public double a() {
    return a;
  }

  public void a(double a) {
    this.a = a;
  }

  public double b() {
    return b;
  }

  public void b(double b) {
    this.b = b;
  }

  @Override
  public Lab clone() {
    try {
      return (Lab) super.clone();
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
    Lab c = (Lab) o;
    return Double.compare(c.l, l) == 0 && Double.compare(c.a, a) == 0
        && Double.compare(c.b, b) == 0;
  }

  @Override
  public void fromArray(double[] array) {
    l = array[0];
    a = array[1];
    b = array[2];
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return l;
    case 1:
      return a;
    case 2:
      return b;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  public double getA() {
    return a;
  }

  public double getB() {
    return b;
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  public double getL() {
    return l;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(l);
    result = 31 * result + Double.hashCode(a);
    result = 31 * result + Double.hashCode(b);
    return result;
  }

  public double l() {
    return l;
  }

  public void l(double l) {
    this.l = l;
  }

  public void setA(double a) {
    this.a = a;
  }

  public void setB(double b) {
    this.b = b;
  }

  public void setL(double l) {
    this.l = l;
  }

  @Override
  public void toArray(double[] array) {
    array[0] = l;
    array[1] = a;
    array[2] = b;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), l, a, b);
  }
}
