package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = "Alpha", shortNames = "A")
public class Alpha extends Color {
  public Alpha() {
    this(1.0);
  }

  public Alpha(double a) {
    setAlpha(a);
  }

  public double getAlpha() {
    return get(0);
  }

  public void setAlpha(double a) {
    set(0, a);
  }

  public double a() {
    return getAlpha();
  }

  public void a(double a) {
    setAlpha(a);
  }

  @Override
  public Alpha clone() {
    return (Alpha) super.clone();
  }
}
