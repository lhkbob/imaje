package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public class RGBToYCbCrTransform implements ColorTransform {
  public static final RGBToYCbCrTransform YCbCr_BT_601 = new RGBToYCbCrTransform(0.299, 0.114, 0.5, 0.5);
  public static final RGBToYCbCrTransform YCbCr_BT_709 = new RGBToYCbCrTransform(0.2126, 0.0722, 0.5, 0.5);
  public static final RGBToYCbCrTransform YCbCr_BT_2020 = new RGBToYCbCrTransform(0.2627, 0.0593, 0.5, 0.5);

  public static final RGBToYCbCrTransform YUV_BT_601 = new RGBToYCbCrTransform(0.299, 0.114, 0.436, 0.615);
  public static final RGBToYCbCrTransform YUV_BT_709 = new RGBToYCbCrTransform(0.2126, 0.0722, 0.436, 0.615);
  public static final RGBToYCbCrTransform YUV_BT_2020 = new RGBToYCbCrTransform(0.2627, 0.0593, 0.436, 0.615);

  private final double kr;
  private final double kb;

  private final double umax;
  private final double vmax;

  public RGBToYCbCrTransform(double kr, double kb, double umax, double vmax) {
    this.kr = kr;
    this.kb = kb;
    this.umax = umax;
    this.vmax = vmax;
  }

  @Override
  public int getInputChannels() {
    return 3;
  }

  @Override
  public int getOutputChannels() {
    return 3;
  }

  @Override
  public YCbCrToRGBTransform inverted() {
    return new YCbCrToRGBTransform(kr, kb, umax, vmax);
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

    // Y from R, G, and B
    output[0] = kr * input[0] + (1.0 - kr - kb) * input[1] + kb * input[2];
    // Cb from Y and B
    output[1] = umax * (input[2] - output[0]) / (1.0 - kb);
    // Cr from Y and R
    output[2] = vmax * (input[0] - output[0]) / (1.0 - kr);
  }
}
