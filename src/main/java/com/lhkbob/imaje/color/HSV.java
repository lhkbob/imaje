package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({HSV.HUE, HSV.SATURATION, HSV.VALUE})
public class HSV implements Color {
  public static final String HUE = "Hue";
  public static final String SATURATION = "Saturation";
  public static final String VALUE = "Value";

  private double hue;
  private double saturation;
  private double value;

  public HSV() {
    this(0.0, 0.0, 0.0);
  }

  public HSV(double h, double s, double v) {
    hue = h;
    saturation = s;
    value = v;
  }

  public double h() {
    return hue;
  }

  public void h(double h) {
    hue = h;
  }

  public double s() {
    return saturation;
  }

  public void s(double s) {
    saturation = s;
  }

  public double v() {
    return value;
  }

  public void v(double v) {
    value = v;
  }

  public double getHue() {
    return hue;
  }

  public void setHue(double hue) {
    this.hue = hue;
  }

  public double getSaturation() {
    return saturation;
  }

  public void setSaturation(double saturation) {
    this.saturation = saturation;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double v) {
    value = v;
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
      return saturation;
    case 2:
      return value;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = hue;
    array[1] = saturation;
    array[2] = value;
  }

  @Override
  public void fromArray(double[] array) {
    hue = array[0];
    saturation = array[1];
    value = array[2];
  }

  @Override
  public HSV clone() {
    try {
      return (HSV) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(hue);
    result = 31 * result + Double.hashCode(saturation);
    result = 31 * result + Double.hashCode(value);
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
    HSV h = (HSV) o;
    return Double.compare(h.hue, hue) == 0 && Double.compare(h.value, value) == 0
        && Double.compare(h.saturation, saturation) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), hue, saturation, value);
  }
}
