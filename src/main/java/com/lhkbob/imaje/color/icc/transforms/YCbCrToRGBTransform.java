package com.lhkbob.imaje.color.icc.transforms;

/**
 * YCbCr's range is [0, 1], [-0.5, 0.5], [-0.5, 0.5]
 */
public class YCbCrToRGBTransform implements ColorTransform {
  public static final YCbCrToRGBTransform YCbCr_BT_601 = RGBToYCbCrTransform.YCbCr_BT_601.inverted();
  public static final YCbCrToRGBTransform YCbCr_BT_709 = RGBToYCbCrTransform.YCbCr_BT_709.inverted();
  public static final YCbCrToRGBTransform YCbCr_BT_2020 = RGBToYCbCrTransform.YCbCr_BT_2020.inverted();

  public static final YCbCrToRGBTransform YUV_BT_601 = RGBToYCbCrTransform.YUV_BT_601.inverted();
  public static final YCbCrToRGBTransform YUV_BT_709 = RGBToYCbCrTransform.YUV_BT_709.inverted();
  public static final YCbCrToRGBTransform YUV_BT_2020 = RGBToYCbCrTransform.YUV_BT_2020.inverted();

  private final double kr;
  private final double kb;

  private final double umax;
  private final double vmax;

  public YCbCrToRGBTransform(double kr, double kb, double umax, double vmax) {
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
  public RGBToYCbCrTransform inverted() {
    return new RGBToYCbCrTransform(kr, kb, umax, vmax);
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

    // R from Y and Cr
    output[0] = (1.0 - kr) * input[0] * input[2] / vmax;
    // B from Y and Cb
    output[2] = (1.0 - kb) * input[0] * input[1] / vmax;
    // G from Y, R, and B
    output[1] = (input[0] - kr * output[0] - kb * output[2]) / (1.0 - kr - kb);
  }
}
