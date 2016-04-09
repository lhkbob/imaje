package com.lhkbob.imaje.color;

import java.util.Arrays;

/**
 *
 */
public abstract class SimpleColor implements Color {
  protected final double[] channels;

  public SimpleColor(int channelCount) {
    this.channels = new double[channelCount];
  }

  public double[] getChannelData() {
    return channels;
  }

  @Override
  public SimpleColor clone() {
    try {
      return (SimpleColor) super.clone();
    } catch(CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen", e);
    }
  }

  @Override
  public void fromArray(double[] array, int offset) {
    if (array.length - offset < channels.length)
      throw new IllegalArgumentException(String
          .format("Array length (%d) not big enough given offset (%d), requires %d elements", array.length, offset, channels.length));
    System.arraycopy(array, offset, channels, 0, channels.length);
  }

  @Override
  public double get(int channel) {
    return channels[channel];
  }

  @Override
  public int getChannelCount() {
    return channels.length;
  }

  @Override
  public void toArray(double[] array, int offset) {
    if (array.length - offset < channels.length)
      throw new IllegalArgumentException(String.format("Array length (%d) not big enough given offset (%d), requires %d elements", array.length, offset, channels.length));

    System.arraycopy(channels, 0, array, offset, channels.length);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() ^ Arrays.hashCode(channels);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (o == null || !o.getClass().equals(getClass())) {
      return false;
    }
    return Arrays.equals(channels, ((SimpleColor) o).channels);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + Arrays.toString(channels);
  }
}
