package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels({ "L", "a", "b" })
public abstract class Lab extends Color {
  public static class CIE extends Lab {
    public CIE() {

    }

    public CIE(double l, double a, double b) {
      set(l, a, b);
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
      set(l, a, b);
    }

    @Override
    public Hunter clone() {
      return (Hunter) super.clone();
    }
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
    return get(1);
  }

  public double getB() {
    return get(2);
  }

  public double getL() {
    return get(0);
  }

  public double l() {
    return getL();
  }

  public void l(double l) {
    setL(l);
  }

  public void setA(double a) {
    set(1, a);
  }

  public void setB(double b) {
    set(2, b);
  }

  public void setL(double l) {
    set(0, l);
  }
}
