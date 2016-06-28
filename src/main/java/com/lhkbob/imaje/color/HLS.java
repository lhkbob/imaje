package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = { "Hue", "Lightness", "Saturation" }, shortNames = { "H", "L", "S"})
public class HLS extends Color {

  public HLS() {
    this(0.0, 0.0, 0.0);
  }

  public HLS(double h, double l, double s) {
    set(h, l, s);
  }

  @Override
  public HLS clone() {
    return (HLS) super.clone();
  }

  public double getHue() {
    return get(0);
  }

  public double getLightness() {
    return get(1);
  }

  public double getSaturation() {
    return get(2);
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
    set(0, hue);
  }

  public void setLightness(double lightness) {
    set(1, lightness);
  }

  public void setSaturation(double saturation) {
    set(2, saturation);
  }
}
