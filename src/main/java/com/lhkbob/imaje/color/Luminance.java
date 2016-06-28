package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = "Luminance", shortNames = "Y")
public class Luminance extends Color {
  public Luminance() {
    this(0.0);
  }

  public Luminance(double l) {
    setLuminance(l);
  }

  @Override
  public Luminance clone() {
    return (Luminance) super.clone();
  }

  public double getLuminance() {
    return get(0);
  }

  public double l() {
    return getLuminance();
  }

  public void l(double l) {
    setLuminance(l);
  }

  public void setLuminance(double l) {
    set(0, l);
  }
}
