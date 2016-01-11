package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ LMS.LONG, LMS.MEDIUM, LMS.SHORT })
public class LMS implements Color {
  public static final String LONG = "Long";
  public static final String MEDIUM = "Medium";
  public static final String SHORT = "Short";

  private double l;
  private double m;
  private double s;

  public LMS() {
    this(0.0, 0.0, 0.0);
  }

  public LMS(double l, double m, double s) {
    this.l = l;
    this.m = m;
    this.s = s;
  }

  public double l() {
    return l;
  }

  public void l(double l) {
    this.l = l;
  }

  public double m() {
    return m;
  }

  public void m(double m) {
    this.m = m;
  }

  public double s() {
    return s;
  }

  public void s(double s) {
    this.s = s;
  }

  public double getLong() {
    return l;
  }

  public void setLong(double l) {
    this.l = l;
  }

  public double getMedium() {
    return m;
  }

  public void setMedium(double m) {
    this.m = m;
  }

  public double getShort() {
    return s;
  }

  public void setShort(double s) {
    this.s = s;
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return l;
    case 1:
      return m;
    case 2:
      return s;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = l;
    array[1] = m;
    array[2] = s;
  }

  @Override
  public void fromArray(double[] array) {
    l = array[0];
    m = array[1];
    s = array[2];
  }

  @Override
  public LMS clone() {
    try {
      return (LMS) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(l);
    result = 31 * result + Double.hashCode(m);
    result = 31 * result + Double.hashCode(s);
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
    LMS c = (LMS) o;
    return Double.compare(c.l, l) == 0 && Double.compare(c.m, m) == 0
        && Double.compare(c.s, s) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), l, m, s);
  }
}
