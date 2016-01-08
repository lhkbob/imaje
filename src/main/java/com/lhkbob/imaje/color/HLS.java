package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Hue", "Lightness", "Saturation" })
public class HLS implements Color {
  private double hue;
  private double lightness;
  private double saturation;

  public HLS() {
    this(0.0, 0.0, 0.0);
  }

  public HLS(double h, double l, double s) {
    hue = h;
    lightness = l;
    saturation = s;
  }

  public double h() {
    return hue;
  }

  public void h(double h) {
    hue = h;
  }

  public double l() {
    return lightness;
  }

  public void l(double l) {
    lightness = l;
  }

  public double s() {
    return saturation;
  }

  public void s(double s) {
    saturation = s;
  }

  public double getHue() {
    return hue;
  }

  public void setHue(double hue) {
    this.hue = hue;
  }

  public double getLightness() {
    return lightness;
  }

  public void setLightness(double lightness) {
    this.lightness = lightness;
  }

  public double getSaturation() {
    return saturation;
  }

  public void setSaturation(double saturation) {
    this.saturation = saturation;
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return hue;
    case 1:
      return lightness;
    case 2:
      return saturation;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = hue;
    array[1] = lightness;
    array[2] = saturation;
  }

  @Override
  public void fromArray(double[] array) {
    hue = array[0];
    lightness = array[1];
    saturation = array[2];
  }

  @Override
  public HLS clone() {
    try {
      return (HLS) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(hue);
    result = 31 * result + Double.hashCode(lightness);
    result = 31 * result + Double.hashCode(saturation);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    HLS h = (HLS) o;
    return Double.compare(h.hue, hue) == 0 && Double.compare(h.lightness, lightness) == 0
        && Double.compare(h.saturation, saturation) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), hue, lightness, saturation);
  }
}