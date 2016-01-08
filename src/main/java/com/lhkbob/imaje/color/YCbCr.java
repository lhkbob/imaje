package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Y", "Cb", "Cr" })
public class YCbCr implements Color {
  private double y;
  private double cb;
  private double cr;

  public YCbCr() {
    this(0.0, 0.0, 0.0);
  }

  public YCbCr(double y, double cb, double cr) {
    this.y = y;
    this.cb = cb;
    this.cr = cr;
  }

  public double y() {
    return y;
  }

  public void y(double y) {
    this.y = y;
  }

  public double cb() {
    return cb;
  }

  public void cb(double cb) {
    this.cb = cb;
  }

  public double cr() {
    return cr;
  }

  public void cr(double cr) {
    this.cr = cr;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getCb() {
    return cb;
  }

  public void setCb(double cb) {
    this.cb = cb;
  }

  public double getCr() {
    return cr;
  }

  public void setCr(double cr) {
    this.cr = cr;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(y);
    result = 31 * result + Double.hashCode(cb);
    result = 31 * result + Double.hashCode(cr);
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
    YCbCr c = (YCbCr) o;
    return Double.compare(c.y, y) == 0 && Double.compare(c.cb, cb) == 0
        && Double.compare(c.cr, cr) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), y, cb, cr);
  }

  public static class REC709 extends YCbCr {
    public REC709() {

    }

    public REC709(double y, double cb, double cr) {
      super(y, cb, cr);
    }

    @Override
    public REC709 clone() {
      return (REC709) super.clone();
    }
  }

  public static class REC2020 extends YCbCr {
    public REC2020() {

    }

    public REC2020(double y, double cb, double cr) {
      super(y, cb, cr);
    }

    @Override
    public REC2020 clone() {
      return (REC2020) super.clone();
    }
  }

  public static class REC601 extends YCbCr {
    public REC601() {

    }

    public REC601(double y, double cb, double cr) {
      super(y, cb, cr);
    }

    @Override
    public REC601 clone() {
      return (REC601) super.clone();
    }
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return y;
    case 1:
      return cb;
    case 2:
      return cr;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = y;
    array[1] = cb;
    array[2] = cr;
  }

  @Override
  public void fromArray(double[] array) {
    y = array[0];
    cb = array[1];
    cr = array[2];
  }

  @Override
  public YCbCr clone() {
    try {
      return (YCbCr) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }
}
