package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;
import com.lhkbob.imaje.color.annot.OpponentAxis;

/**
 *
 */
@Channels({ "Y", "Cb", "Cr" })
public abstract class YCbCr extends Color {
  @OpponentAxis(aWeight = 0.0593, bWeight = 0.2627)
  public static class REC2020 extends YCbCr {
    public REC2020() {

    }

    public REC2020(double y, double cb, double cr) {
      set(y, cb, cr);
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
      set(y, cb, cr);
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
      set(y, cb, cr);
    }

    @Override
    public REC709 clone() {
      return (REC709) super.clone();
    }
  }

  @Override
  public YCbCr clone() {
    return (YCbCr) super.clone();
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
    return get(1);
  }

  public double getCr() {
    return get(2);
  }

  public double getY() {
    return get(0);
  }

  public void setCb(double cb) {
    set(1, cb);
  }

  public void setCr(double cr) {
    set(2, cr);
  }

  public void setY(double y) {
    set(0, y);
  }

  public double y() {
    return getY();
  }

  public void y(double y) {
    setY(y);
  }
}
