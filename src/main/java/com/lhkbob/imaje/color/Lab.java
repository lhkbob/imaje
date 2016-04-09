package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "L", "a", "b" })
public class Lab extends SimpleColor {
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

  public Lab() {
    this(0, 0, 0);
  }

  public Lab(double l, double a, double b) {
    super(3);
    set(l, a, b);
  }

  public double a() {
    return getA();
  }

  public void a(double a) {
    setA(a);
  }

  public double b() {
    return getB();
  }

  public void b(double b) {
    setB(b);
  }

  @Override
  public Lab clone() {
    return (Lab) super.clone();
  }

  public double getA() {
    return channels[1];
  }

  public double getB() {
    return channels[2];
  }

  public double getL() {
    return channels[0];
  }

  public double l() {
    return getL();
  }

  public void l(double l) {
    setL(l);
  }

  public void setA(double a) {
    channels[1] = a;
  }

  public void setB(double b) {
    channels[2] = b;
  }

  public void setL(double l) {
    channels[0] = l;
  }
}
