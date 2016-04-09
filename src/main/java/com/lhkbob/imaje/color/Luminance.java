package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Y" })
public class Luminance extends SimpleColor {
  public Luminance() {
    this(0.0);
  }

  public Luminance(double l) {
    super(1);
    set(l);
  }

  @Override
  public Luminance clone() {
    return (Luminance) super.clone();
  }

  public double getLuminance() {
    return channels[0];
  }

  public double l() {
    return getLuminance();
  }

  public void l(double l) {
    setLuminance(l);
  }

  public void setLuminance(double l) {
    channels[0] = l;
  }
}
