package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Y'", "U", "V" })
public class YUV implements Color {
  private double y;
  private double u;
  private double v;

  public YUV() {
    this(0.0, 0.0, 0.0);
  }

  public YUV(double y, double u, double v) {
    this.y = y;
    this.u = u;
    this.v = v;
  }

  public double y() {
    return y;
  }

  public void y(double y) {
    this.y = y;
  }

  public double u() {
    return u;
  }

  public void u(double u) {
    this.u = u;
  }

  public double v() {
    return v;
  }

  public void v(double v) {
    this.v = v;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getU() {
    return u;
  }

  public void setU(double u) {
    this.u = u;
  }

  public double getV() {
    return v;
  }

  public void setV(double v) {
    this.v = v;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(y);
    result = 31 * result + Double.hashCode(u);
    result = 31 * result + Double.hashCode(v);
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
    YUV c = (YUV) o;
    return Double.compare(c.y, y) == 0 && Double.compare(c.u, u) == 0
        && Double.compare(c.v, v) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), y, u, v);
  }

  @OpponentAxis(aWeight = 0.0722, bWeight = 0.2126)
  public static class REC709 extends YUV {
    public REC709() {

    }

    public REC709(double y, double u, double v) {
      super(y, u, v);
    }

    @Override
    public REC709 clone() {
      return (REC709) super.clone();
    }
  }

  @OpponentAxis(aWeight = 0.0593, bWeight = 0.2627)
  public static class REC2020 extends YUV {
    public REC2020() {

    }

    public REC2020(double y, double u, double v) {
      super(y, u, v);
    }

    @Override
    public REC2020 clone() {
      return (REC2020) super.clone();
    }
  }

  @OpponentAxis(aWeight = 0.114, bWeight = 0.299)
  public static class REC601 extends YUV {
    public REC601() {

    }

    public REC601(double y, double u, double v) {
      super(y, u, v);
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
      return u;
    case 2:
      return v;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = y;
    array[1] = u;
    array[2] = v;
  }

  @Override
  public void fromArray(double[] array) {
    y = array[0];
    u = array[1];
    v = array[2];
  }

  @Override
  public YUV clone() {
    try {
      return (YUV) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }
}
