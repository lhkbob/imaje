package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = { "Depth", "Stencil Mask" }, shortNames = { "D", "S" })
public abstract class DepthStencil extends Color {
  public static class Device extends DepthStencil {
    public Device() {

    }

    public Device(double depth, int stencil) {
      set(depth, stencil);
    }

    @Override
    public Device clone() {
      return (Device) super.clone();
    }
  }

  public static class Linear extends DepthStencil {
    public Linear() {

    }

    public Linear(double depth, int stencil) {
      set(depth, stencil);
    }

    @Override
    public Linear clone() {
      return (Linear) super.clone();
    }
  }

  @Override
  public DepthStencil clone() {
    return (DepthStencil) super.clone();
  }

  public double getDepth() {
    return get(0);
  }

  public int getStencil() {
    return (int) get(1);
  }

  public void setDepth(double depth) {
    set(0, depth);
  }

  public void setStencil(int stencil) {
    set(1, stencil);
  }

  public int stencil() {
    return getStencil();
  }

  public void stencil(int stencil) {
    setStencil(stencil);
  }

  public double z() {
    return getDepth();
  }

  public void z(double z) {
    setDepth(z);
  }
}
