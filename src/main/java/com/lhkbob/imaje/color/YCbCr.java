package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Y", "Cb", "Cr" })
public class YCbCr extends SimpleColor {
  @OpponentAxis(aWeight = 0.0593, bWeight = 0.2627)
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

  @OpponentAxis(aWeight = 0.114, bWeight = 0.299)
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

  @OpponentAxis(aWeight = 0.0722, bWeight = 0.2126)
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

  public YCbCr() {
    this(0.0, 0.0, 0.0);
  }

  public YCbCr(double y, double cb, double cr) {
    super(3);
    set(y, cb, cr);
  }

  public double cb() {
    return getCb();
  }

  public void cb(double cb) {
    setCb(cb);
  }

  public double cr() {
    return getCr();
  }

  public void cr(double cr) {
    setCr(cr);
  }

  public double getCb() {
    return channels[1];
  }

  public double getCr() {
    return channels[2];
  }

  public double getY() {
    return channels[0];
  }

  public void setCb(double cb) {
    channels[1] = cb;
  }

  public void setCr(double cr) {
    channels[2] = cr;
  }

  public void setY(double y) {
    channels[0] = y;
  }

  public double y() {
    return getY();
  }

  public void y(double y) {
    setY(y);
  }
}
