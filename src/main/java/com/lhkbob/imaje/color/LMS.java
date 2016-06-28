package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = { "Long", "Medium", "Short" }, shortNames = {"L", "M", "S"})
public class LMS extends Color {
  public LMS() {
    this(0.0, 0.0, 0.0);
  }

  public LMS(double l, double m, double s) {
    set(l, m, s);
  }

  @Override
  public LMS clone() {
    return (LMS) super.clone();
  }

  public double getLong() {
    return get(0);
  }

  public double getMedium() {
    return get(1);
  }

  public double getShort() {
    return get(2);
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
    set(0, l);
  }

  public void setMedium(double m) {
    set(1, m);
  }

  public void setShort(double s) {
    set(2, s);
  }
}
