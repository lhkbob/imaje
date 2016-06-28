package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels({ "L", "u", "v" })
public class Luv extends Color {

  public Luv() {
    this(0, 0, 0);
  }

  public Luv(double l, double u, double v) {
    set(l, u, v);
  }

  @Override
  public Luv clone() {
    return (Luv) super.clone();
  }

  public double getL() {
    return get(0);
  }

  public double getU() {
    return get(1);
  }

  public double getV() {
    return get(2);
  }

  public double l() {
    return getL();
  }

  public void l(double l) {
    setL(l);
  }

  public void setL(double l) {
    set(0, l);
  }

  public void setU(double u) {
    set(1, u);
  }

  public void setV(double v) {
    set(2, v);
  }

  public double u() {
    return getU();
  }

  public void u(double u) {
    setU(u);
  }

  public double v() {
    return getV();
  }

  public void v(double v) {
    setV(v);
  }
}
