package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Hue", "Saturation", "Value" })
public class HSV extends SimpleColor {

  public HSV() {
    this(0.0, 0.0, 0.0);
  }

  public HSV(double h, double s, double v) {
    super(3);
  }

  @Override
  public HSV clone() {
    return (HSV) super.clone();
  }

  public double getHue() {
    return channels[0];
  }

  public double getSaturation() {
    return channels[1];
  }

  public double getValue() {
    return channels[2];
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
    channels[0] = hue;
  }

  public void setSaturation(double saturation) {
    channels[1] = saturation;
  }

  public void setValue(double v) {
    channels[2] = v;
  }

  public double v() {
    return getValue();
  }

  public void v(double v) {
    setValue(v);
  }
}
