package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Long", "Medium", "Short" })
public class LMS extends SimpleColor {
  public LMS() {
    this(0.0, 0.0, 0.0);
  }

  public LMS(double l, double m, double s) {
    super(3);
    set(l, m, s);
  }

  @Override
  public LMS clone() {
    return (LMS) super.clone();
  }

  public double getLong() {
    return channels[0];
  }

  public double getMedium() {
    return channels[1];
  }

  public double getShort() {
    return channels[2];
  }

  public double l() {
    return getLong();
  }

  public void l(double l) {
    setLong(l);
  }

  public double m() {
    return getMedium();
  }

  public void m(double m) {
    setMedium(m);
  }

  public double s() {
    return getShort();
  }

  public void s(double s) {
    setShort(s);
  }

  public void setLong(double l) {
    channels[0] = l;
  }

  public void setMedium(double m) {
    channels[1] = m;
  }

  public void setShort(double s) {
    channels[2] = s;
  }
}
