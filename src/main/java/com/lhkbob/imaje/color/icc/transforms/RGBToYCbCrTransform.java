package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public class RGBToYCbCrTransform implements ColorTransform {
  public static final RGBToYCbCrTransform YCbCr_BT_601 = new RGBToYCbCrTransform(
      0.299, 0.114, 0.5, 0.5);
  public static final RGBToYCbCrTransform YCbCr_BT_709 = new RGBToYCbCrTransform(
      0.2126, 0.0722, 0.5, 0.5);
  public static final RGBToYCbCrTransform YCbCr_BT_2020 = new RGBToYCbCrTransform(
      0.2627, 0.0593, 0.5, 0.5);

  public static final RGBToYCbCrTransform YUV_BT_601 = new RGBToYCbCrTransform(
      0.299, 0.114, 0.436, 0.615);
  public static final RGBToYCbCrTransform YUV_BT_709 = new RGBToYCbCrTransform(
      0.2126, 0.0722, 0.436, 0.615);
  public static final RGBToYCbCrTransform YUV_BT_2020 = new RGBToYCbCrTransform(
      0.2627, 0.0593, 0.436, 0.615);

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

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Double.hashCode(kr);
    result = 31 * result + Double.hashCode(kb);
    result = 31 * result + Double.hashCode(umax);
    result = 31 * result + Double.hashCode(vmax);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof RGBToYCbCrTransform)) {
      return false;
    }
    RGBToYCbCrTransform c = (RGBToYCbCrTransform) o;
    return Double.compare(c.kr, kr) == 0 && Double.compare(c.kb, kb) == 0
        && Double.compare(c.umax, umax) == 0 && Double.compare(c.vmax, vmax) == 0;
  }

  @Override
  public String toString() {
    if (YCbCr_BT_601.equals(this)) {
      return "RGB -> YCbCr (ITU Rec BT 601) Transform";
    } else if (YCbCr_BT_709.equals(this)) {
      return "RGB -> YCbCr (ITU Rec BT 709) Transform";
    } else if (YCbCr_BT_2020.equals(this)) {
      return "RGB -> YCbCr (ITU Rec BT 2020) Transform";
    } else if (YUV_BT_601.equals(this)) {
      return "RGB -> YUV (ITU Rec BT 601) Transform";
    } else if (YUV_BT_709.equals(this)) {
      return "RGB -> YUV (ITU Rec BT 709) Transform";
    } else if (YUV_BT_2020.equals(this)) {
      return "RGB -> YUV (ITU Rec BT 2020) Transform";
    } else {
      return String
          .format("RGB -> Yb*r* (kb: %.3f, kr: %.3f, b-max: %.3f, r-max: %.3f)", kb, kr, umax,
              vmax);
    }
  }
}
