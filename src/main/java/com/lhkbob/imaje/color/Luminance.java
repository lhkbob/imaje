package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ Luminance.LUMINANCE })
public class Luminance implements Color {
  public static final String LUMINANCE = "Luminance";

  private double l;

  public Luminance() {
    this(0.0);
  }

  public Luminance(double l) {
    this.l = l;
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
  public void fromArray(double[] array) {
    l = array[0];
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
  public int getChannelCount() {
    return 1;
  }

  public double getLuminance() {
    return l;
  }

  @Override
  public int hashCode() {
    return Double.hashCode(l);
  }

  public double l() {
    return l;
  }

  public void l(double l) {
    this.l = l;
  }

  public void setLuminance(double l) {
    this.l = l;
  }

  @Override
  public void toArray(double[] array) {
    array[0] = l;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f)", getClass().getSimpleName(), l);
  }
}
