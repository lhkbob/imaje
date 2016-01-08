package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Luminance" })
public class Luminance implements Color {
  private double l;

  public Luminance() {
    this(0.0);
  }

  public Luminance(double l) {
    this.l = l;
  }

  public double l() {
    return l;
  }

  public void l(double l) {
    this.l = l;
  }

  public double getLuminance() {
    return l;
  }

  public void setLuminance(double l) {
    this.l = l;
  }

  @Override
  public int getChannelCount() {
    return 1;
  }

  @Override
  public double get(int channel) {
    if (channel == 0) {
      return l;
    } else {
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = l;
  }

  @Override
  public void fromArray(double[] array) {
    l = array[0];
  }

  @Override
  public Luminance clone() {
    try {
      return (Luminance) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public int hashCode() {
    return Double.hashCode(l);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    return Double.compare(((Luminance) o).l, l) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f)", getClass().getSimpleName(), l);
  }
}
