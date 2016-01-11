package com.lhkbob.imaje.color.transform.general;

import java.util.Arrays;

/**
 *
 */
public class NormalizeChannels implements Transform {
  private final double[] channelMins;
  private final double[] channelMaxs;

  public NormalizeChannels(double[] channelMins, double[] channelMaxs) {
    if (channelMins.length != channelMaxs.length) {
      throw new IllegalArgumentException(
          "Channel minimum and maximum value arrays must be of same length");
    }
    this.channelMaxs = new double[channelMaxs.length];
    this.channelMins = new double[channelMins.length];
    for (int i = 0; i < channelMaxs.length; i++) {
      if (channelMins[i] > channelMaxs[i]) {
        throw new IllegalArgumentException(
            "Minimum value in channel must be less than or equal to max value");
      }
      this.channelMaxs[i] = channelMaxs[i];
      this.channelMins[i] = channelMins[i];
    }
  }

  @Override
  public int getInputChannels() {
    return channelMaxs.length;
  }

  @Override
  public int getOutputChannels() {
    return channelMaxs.length;
  }

  @Override
  public Transform inverted() {
    return new DenormalizeChannelTransform();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    for (int i = 0; i < channelMins.length; i++) {
      double unclipped = (input[i] - channelMins[i]) / (channelMaxs[i] - channelMins[i]);
      output[i] = Math.max(0.0, Math.min(unclipped, 1.0));
    }
  }

  @Override
  public NormalizeChannels getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Arrays.hashCode(channelMaxs);
    result = 31 * result + Arrays.hashCode(channelMins);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof NormalizeChannels)) {
      return false;
    }
    NormalizeChannels c = (NormalizeChannels) o;
    return Arrays.equals(c.channelMins, channelMins) && Arrays.equals(c.channelMaxs, channelMaxs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Normalize Channels Transform (dim: ")
        .append(channelMaxs.length).append("):");
    for (int i = 0; i < channelMaxs.length; i++) {
      sb.append("\n  channel ").append(i + 1).append(": ")
          .append(String.format("[%.3f, %.3f] -> [0.0, 1.0]", channelMins[i], channelMaxs[i]));
    }
    return sb.toString();
  }

  private class DenormalizeChannelTransform implements Transform {
    @Override
    public int getInputChannels() {
      return channelMaxs.length;
    }

    @Override
    public int getOutputChannels() {
      return channelMaxs.length;
    }

    @Override
    public Transform inverted() {
      return NormalizeChannels.this;
    }

    @Override
    public void transform(double[] input, double[] output) {
      Transform.validateDimensions(this, input, output);

      for (int i = 0; i < channelMaxs.length; i++) {
        double clipped = Math.max(0.0, Math.min(input[i], 1.0));
        output[i] = clipped * (channelMaxs[i] - channelMins[i]) + channelMins[i];
      }
    }

    @Override
    public int hashCode() {
      return DenormalizeChannelTransform.class.hashCode() ^ NormalizeChannels.this
          .hashCode();
    }

    @Override
    public DenormalizeChannelTransform getLocallySafeInstance() {
      // This is purely functional so the instance can be used by any thread
      return this;
    }

    private NormalizeChannels getParent() {
      return NormalizeChannels.this;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof DenormalizeChannelTransform)) {
        return false;
      }
      return ((DenormalizeChannelTransform) o).getParent().equals(getParent());
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("De-normalize Channels Transform (dim: ")
          .append(channelMaxs.length).append("):");
      for (int i = 0; i < channelMaxs.length; i++) {
        sb.append("\n  channel ").append(i + 1).append(": ")
            .append(String.format("[0.0, 1.0] -> [%.3f, %.3f]", channelMins[i], channelMaxs[i]));
      }
      return sb.toString();
    }
  }
}
