package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = { "Cyan", "Magenta", "Yellow", "Key" }, shortNames = { "C", "M", "Y", "K"})
public class CMYK extends Color {

  public CMYK() {
    this(0.0, 0.0, 0.0, 1.0);
  }

  public CMYK(double c, double m, double y, double k) {
    set(c, m, y, k);
  }

  public double c() {
    return getCyan();
  }

  public void c(double c) {
    setCyan(c);
  }

  @Override
  public CMYK clone() {
    return (CMYK) super.clone();
  }

  public double getCyan() {
    return get(0);
  }

  public double getKey() {
    return get(3);
  }

  public double getMagenta() {
    return get(1);
  }

  public double getYellow() {
    return get(2);
  }

  public double k() {
    return getKey();
  }

  public void k(double k) {
    setKey(k);
  }

  public double m() {
    return getMagenta();
  }

  public void m(double m) {
    setMagenta(m);
  }

  public void setCyan(double cyan) {
    set(0, cyan);
  }

  public void setKey(double key) {
    set(3, key);
  }

  public void setMagenta(double magenta) {
    set(1, magenta);
  }

  public void setYellow(double yellow) {
    set(2, yellow);
  }

  public double y() {
    return getYellow();
  }

  public void y(double y) {
    setYellow(y);
  }
}
