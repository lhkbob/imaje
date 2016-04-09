package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Y", "U", "V" })
public class YUV extends SimpleColor {
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

  public YUV() {
    this(0.0, 0.0, 0.0);
  }

  public YUV(double y, double u, double v) {
    super(3);
    set(y, u, v);
  }

  @Override
  public YUV clone() {
    return (YUV) super.clone();
  }

  public double getU() {
    return channels[1];
  }

  public double getV() {
    return channels[2];
  }

  public double getY() {
    return channels[0];
  }

  public void setU(double u) {
    channels[1] = u;
  }

  public void setV(double v) {
    channels[2] = v;
  }

  public void setY(double y) {
    channels[0] = y;
  }

  public double u() {
    return getU();
  }

  public void u(double u) {
    setU(u);
  }

  public double v() {
    return getV();
  }

  public void v(double v) {
    setV(v);
  }

  public double y() {
    return getY();
  }

  public void y(double y) {
    setY(y);
  }
}
