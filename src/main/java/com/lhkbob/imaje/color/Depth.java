package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = "Depth", shortNames = "D")
public abstract class Depth extends Color {
  public static class Device extends Depth {
    public Device() {

    }

    public Device(double d) {
      setDepth(d);
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
      setDepth(d);
    }

    @Override
    public Linear clone() {
      return (Linear) super.clone();
    }
  }

  @Override
  public Depth clone() {
    return (Depth) super.clone();
  }

  public double getDepth() {
    return get(0);
  }

  public void setDepth(double d) {
    set(0, d);
  }

  public double z() {
    return getDepth();
  }

  public void z(double z) {
    setDepth(z);
  }
}
