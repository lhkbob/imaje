package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public interface Transform {
  int getInputChannels();

  int getOutputChannels();

  Transform inverted();

  void transform(double[] input, double[] output);

  Transform getLocallySafeInstance();

  static void validateDimensions(Transform t, double[] input, double[] output) {
    if (input.length != t.getInputChannels()) {
      throw new IllegalArgumentException(
          "Input vector must have " + t.getInputChannels() + " channels, but has " + input.length);
    }
    if (output.length != t.getOutputChannels()) {
      throw new IllegalArgumentException(
          "Output vector must have " + t.getOutputChannels() + " channels, but has " + output.length);
    }
  }
}
