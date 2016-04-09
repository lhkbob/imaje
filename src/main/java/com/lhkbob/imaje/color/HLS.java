package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Hue", "Lightness", "Saturation" })
public class HLS extends SimpleColor {

  public HLS() {
    this(0.0, 0.0, 0.0);
  }

  public HLS(double h, double l, double s) {
    super(3);
    set(h, l, s);
  }

  @Override
  public HLS clone() {
    return (HLS) super.clone();
  }

  public double getHue() {
    return channels[0];
  }

  public double getLightness() {
    return channels[1];
  }

  public double getSaturation() {
    return channels[2];
  }

  public double h() {
    return getHue();
  }

  public void h(double h) {
    setHue(h);
  }

  public double l() {
    return getLightness();
  }

  public void l(double l) {
    setLightness(l);
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

  public void setLightness(double lightness) {
    channels[1] = lightness;
  }

  public void setSaturation(double saturation) {
    channels[2] = saturation;
  }
}
