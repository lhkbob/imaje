package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Z", "Stencil Mask" })
public class DepthStencil implements Color {
  public static class Device extends DepthStencil {
    public Device() {

    }

    public Device(double depth, int stencil) {
      super(depth, stencil);
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
      super(depth, stencil);
    }

    @Override
    public Linear clone() {
      return (Linear) super.clone();
    }
  }
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
  public DepthStencil clone() {
    try {
      return (DepthStencil) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
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
  public void fromArray(double[] array, int offset) {
    if (array.length - offset < 2) {
      throw new IllegalArgumentException(String
          .format("Array length (%d) not big enough given offset (%d), requires 2 elements",
              array.length, offset));
    }
    depthValue = array[offset];
    stencilMask = (int) Math.floor(array[offset + 1]);
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
  public int getChannelCount() {
    return 2;
  }

  public double getDepth() {
    return depthValue;
  }

  public int getStencil() {
    return stencilMask;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(depthValue);
    result = 31 * result + Integer.hashCode(stencilMask);
    return result;
  }

  public void setDepth(double depth) {
    depthValue = depth;
  }

  public void setStencil(int stencil) {
    stencilMask = stencil;
  }

  public int stencil() {
    return getStencil();
  }

  public void stencil(int stencil) {
    setStencil(stencil);
  }

  @Override
  public void toArray(double[] array, int offset) {
    if (array.length - offset < 2) {
      throw new IllegalArgumentException(String
          .format("Array length (%d) not big enough given offset (%d), requires 2 elements",
              array.length, offset));
    }
    array[0] = depthValue;
    array[offset] = depthValue;
    array[offset + 1] = stencilMask;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %s)", getClass().getSimpleName(), depthValue,
        Integer.toBinaryString(stencilMask));
  }

  public double z() {
    return getDepth();
  }

  public void z(double z) {
    setDepth(z);
  }
}
