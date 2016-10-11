package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class Identity implements Transform {
  private final int channels;

  public Identity(int channels) {
    this.channels = channels;
  }

  @Override
  public int getInputChannels() {
    return channels;
  }

  @Override
  public Identity getLocallySafeInstance() {
    return this;
  }

  @Override
  public int getOutputChannels() {
    return channels;
  }

  @Override
  public Identity inverted() {
    return this;
  }

  @Override
  public void transform(double[] input, double[] output) {
    // Copy input to output
    Transform.validateDimensions(this, input, output);
    System.arraycopy(input, 0, output, 0, channels);
  }
}
