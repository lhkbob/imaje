package com.lhkbob.imaje.color;

/**
 *
 */
@Channels("Z")
public class Depth extends SimpleColor {
  public static class Device extends Depth {
    public Device() {

    }

    public Device(double d) {
      super(d);
    }

    @Override
    public Device clone() {
      return (Device) super.clone();
    }
  }

  public static class Linear extends Depth {
    public Linear() {

    }

    public Linear(double d) {
      super(d);
    }

    @Override
    public Linear clone() {
      return (Linear) super.clone();
    }
  }

  public Depth() {
    this(0.0);
  }

  public Depth(double d) {
    super(1);
    set(d);
  }

  @Override
  public Depth clone() {
    return (Depth) super.clone();
  }

  public double getDepth() {
    return channels[0];
  }

  public void setDepth(double d) {
    channels[0] = d;
  }

  public double z() {
    return getDepth();
  }

  public void z(double z) {
    setDepth(z);
  }
}
