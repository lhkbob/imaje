package com.lhkbob.imaje.color;

/**
 *
 */
@Channels("Depth")
public class Depth implements Color {
  private double depth;

  public Depth() {
    this(0.0);
  }

  public Depth(double d) {
    depth = d;
  }

  public double z() {
    return depth;
  }

  public void z(double z) {
    depth = z;
  }

  public double getDepth() {
    return depth;
  }

  public void setDepth(double d) {
    depth = d;
  }

  @Override
  public int getChannelCount() {
    return 1;
  }

  @Override
  public double get(int channel) {
    if (channel == 0) {
      return depth;
    } else {
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = depth;
  }

  @Override
  public void fromArray(double[] array) {
    depth = array[0];
  }

  @Override
  public Depth clone() {
    try {
      return (Depth) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public int hashCode() {
    return Double.hashCode(depth);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    return Double.compare(((Depth) o).depth, depth) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f)", getClass().getSimpleName(), depth);
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

  public static class Normalized extends Depth {
    public Normalized() {

    }

    public Normalized(double d) {
      super(d);
    }

    @Override
    public Normalized clone() {
      return (Normalized) super.clone();
    }
  }
}
