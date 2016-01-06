package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public class NormalizeChannelTransform implements ColorTransform {
  private final double[] channelMins;
  private final double[] channelMaxs;

  public NormalizeChannelTransform(double[] channelMins, double[] channelMaxs) {
    if (channelMins.length != channelMaxs.length) {
      throw new IllegalArgumentException("Channel minimum and maximum value arrays must be of same length");
    }
    this.channelMaxs = new double[channelMaxs.length];
    this.channelMins = new double[channelMins.length];
    for (int i = 0; i < channelMaxs.length; i++) {
      if (channelMins[i] > channelMaxs[i])
        throw new IllegalArgumentException("Minimum value in channel must be less than or equal to max value");
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
  public ColorTransform inverted() {
    return new DenormalizeChannelTransform();
  }

  @Override
  public void transform(double[] input, double[] output) {
    if (input.length != getInputChannels()) {
      throw new IllegalArgumentException(
          "Input vector must have " + getInputChannels() + " channels, but has " + input.length);
    }
    if (output.length != getOutputChannels()) {
      throw new IllegalArgumentException(
          "Output vector must have " + getOutputChannels() + " channels, but has " + output.length);
    }

    for (int i = 0; i < channelMins.length; i++) {
      double unclipped = (input[i] - channelMins[i]) / (channelMaxs[i] - channelMins[i]);
      output[i] = Math.max(0.0, Math.min(unclipped, 1.0));
    }
  }

  private class DenormalizeChannelTransform implements ColorTransform {
    @Override
    public int getInputChannels() {
      return channelMaxs.length;
    }

    @Override
    public int getOutputChannels() {
      return channelMaxs.length;
    }

    @Override
    public ColorTransform inverted() {
      return NormalizeChannelTransform.this;
    }

    @Override
    public void transform(double[] input, double[] output) {
      if (input.length != getInputChannels()) {
        throw new IllegalArgumentException(
            "Input vector must have " + getInputChannels() + " channels, but has " + input.length);
      }
      if (output.length != getOutputChannels()) {
        throw new IllegalArgumentException(
            "Output vector must have " + getOutputChannels() + " channels, but has " + output.length);
      }

      for (int i = 0; i < channelMaxs.length; i++) {
        double clipped = Math.max(0.0, Math.min(input[i], 1.0));
        output[i] = clipped * (channelMaxs[i] - channelMins[i]) + channelMins[i];
      }
    }
  }
}
