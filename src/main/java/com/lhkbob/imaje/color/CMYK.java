package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Cyan", "Magenta", "Yellow", "Key" })
public class CMYK extends SimpleColor {

  public CMYK() {
    this(0.0, 0.0, 0.0, 1.0);
  }

  public CMYK(double c, double m, double y, double k) {
    super(4);
    set(c, m, y, k);
  }

  public double c() {
    return channels[0];
  }

  public void c(double c) {
    channels[0] = c;
  }

  @Override
  public CMYK clone() {
    return (CMYK) super.clone();
  }

  public double getCyan() {
    return channels[0];
  }

  public double getKey() {
    return channels[3];
  }

  public double getMagenta() {
    return channels[1];
  }

  public double getYellow() {
    return channels[2];
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
    channels[0] = cyan;
  }

  public void setKey(double key) {
    channels[3] = key;
  }

  public void setMagenta(double magenta) {
    channels[1] = magenta;
  }

  public void setYellow(double yellow) {
    channels[2] = yellow;
  }

  public double y() {
    return getYellow();
  }

  public void y(double y) {
    setYellow(y);
  }
}
