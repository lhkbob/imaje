package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ DepthStencil.Z, DepthStencil.STENCIL_MASK })
public class DepthStencil implements Color {
  public static final String Z = "Z";
  public static final String STENCIL_MASK = "Stencil";

  private double depthValue;
  private int stencilMask;

  public DepthStencil() {
    this(0.0, 0);
  }

  public DepthStencil(double depthValue, int stencilMask) {
    this.depthValue = depthValue;
    this.stencilMask = stencilMask;
  }

  @Override
  public int getChannelCount() {
    return 2;
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return depthValue;
    case 1:
      return stencilMask;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = depthValue;
    array[1] = stencilMask;
  }

  @Override
  public void fromArray(double[] array) {
    depthValue = array[0];
    stencilMask = (int) Math.floor(array[1]);
  }

  @Override
  public DepthStencil clone() {
    try {
      return (DepthStencil) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  public double z() {
    return depthValue;
  }

  public void z(double z) {
    depthValue = z;
  }

  public int stencil() {
    return stencilMask;
  }

  public void stencil(int stencil) {
    stencilMask = stencil;
  }

  public double getDepth() {
    return depthValue;
  }

  public void setDepth(double depth) {
    depthValue = depth;
  }

  public int getStencil() {
    return stencilMask;
  }

  public void setStencil(int stencil) {
    stencilMask = stencil;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(depthValue);
    result = 31 * result + Integer.hashCode(stencilMask);
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
    DepthStencil d = (DepthStencil) o;
    return Double.compare(d.depthValue, depthValue) == 0 && d.stencilMask == stencilMask;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %s)", getClass().getSimpleName(), depthValue,
        Integer.toBinaryString(stencilMask));
  }

  public static class Linear extends DepthStencil {
    public Linear() {

    }

    public Linear(double depth, int stencil) {
      super(depth, stencil);
    }

    @Override
    public Linear clone() {
      return (Linear) super.clone();
    }
  }

  public static class Normalized extends DepthStencil {
    public Normalized() {

    }

    public Normalized(double depth, int stencil) {
      super(depth, stencil);
    }

    @Override
    public Normalized clone() {
      return (Normalized) super.clone();
    }
  }
}
