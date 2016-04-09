package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "L", "u", "v" })
public class Luv extends SimpleColor {

  public Luv() {
    this(0, 0, 0);
  }

  public Luv(double l, double u, double v) {
    super(3);
    set(l, u, v);
  }

  @Override
  public Luv clone() {
    return (Luv) super.clone();
  }

  public double getL() {
    return channels[0];
  }

  public double getU() {
    return channels[1];
  }

  public double getV() {
    return channels[2];
  }

  public double l() {
    return getL();
  }

  public void l(double l) {
    setL(l);
  }

  public void setL(double l) {
    channels[0] = l;
  }

  public void setU(double u) {
    channels[1] = u;
  }

  public void setV(double v) {
    channels[2] = v;
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
