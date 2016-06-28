package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = { "Hue", "Saturation", "Value" }, shortNames = { "H", "S", "V" })
public class HSV extends Color {

  public HSV() {
    this(0.0, 0.0, 0.0);
  }

  public HSV(double h, double s, double v) {
    set(h, s, v);
  }

  @Override
  public HSV clone() {
    return (HSV) super.clone();
  }

  public double getHue() {
    return get(0);
  }

  public double getSaturation() {
    return get(1);
  }

  public double getValue() {
    return get(2);
  }

  public double h() {
    return getHue();
  }

  public void h(double h) {
    setHue(h);
  }

  public double s() {
    return getSaturation();
  }

  public void s(double s) {
    setSaturation(s);
  }

  public void setHue(double hue) {
    set(0, hue);
  }

  public void setSaturation(double saturation) {
    set(1, saturation);
  }

  public void setValue(double v) {
    set(2, v);
  }

  public double v() {
    return getValue();
  }

  public void v(double v) {
    setValue(v);
  }
}
