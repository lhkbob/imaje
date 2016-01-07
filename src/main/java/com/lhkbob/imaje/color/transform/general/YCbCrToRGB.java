package com.lhkbob.imaje.color.transform.general;

/**
 * YCbCr's range is [0, 1], [-0.5, 0.5], [-0.5, 0.5]
 */
public class YCbCrToRGB implements Transform {
  public static final YCbCrToRGB YCbCr_BT_601 = RGBToYCbCr.YCbCr_BT_601.inverted();
  public static final YCbCrToRGB YCbCr_BT_709 = RGBToYCbCr.YCbCr_BT_709.inverted();
  public static final YCbCrToRGB YCbCr_BT_2020 = RGBToYCbCr.YCbCr_BT_2020.inverted();

  public static final YCbCrToRGB YUV_BT_601 = RGBToYCbCr.YUV_BT_601.inverted();
  public static final YCbCrToRGB YUV_BT_709 = RGBToYCbCr.YUV_BT_709.inverted();
  public static final YCbCrToRGB YUV_BT_2020 = RGBToYCbCr.YUV_BT_2020.inverted();

  private final double kr;
  private final double kb;

  private final double umax;
  private final double vmax;

  public YCbCrToRGB(double kr, double kb, double umax, double vmax) {
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
  public RGBToYCbCr inverted() {
    return new RGBToYCbCr(kr, kb, umax, vmax);
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
    if (!(o instanceof YCbCrToRGB)) {
      return false;
    }
    YCbCrToRGB c = (YCbCrToRGB) o;
    return Double.compare(c.kr, kr) == 0 && Double.compare(c.kb, kb) == 0
        && Double.compare(c.umax, umax) == 0 && Double.compare(c.vmax, vmax) == 0;
  }

  @Override
  public String toString() {
    if (YCbCr_BT_601.equals(this)) {
      return "YCbCr (ITU Rec BT 601) -> RGB Transform";
    } else if (YCbCr_BT_709.equals(this)) {
      return "YCbCr (ITU Rec BT 709) -> RGB Transform";
    } else if (YCbCr_BT_2020.equals(this)) {
      return "YCbCr (ITU Rec BT 2020) -> RGB Transform";
    } else if (YUV_BT_601.equals(this)) {
      return "YUV (ITU Rec BT 601) -> RGB Transform";
    } else if (YUV_BT_709.equals(this)) {
      return "YUV (ITU Rec BT 709) -> RGB Transform";
    } else if (YUV_BT_2020.equals(this)) {
      return "YUV (ITU Rec BT 2020) -> RGB Transform";
    } else {
      return String
          .format("Yb*r* -> RGB (kb: %.3f, kr: %.3f, b-max: %.3f, r-max: %.3f)", kb, kr, umax,
              vmax);
    }
  }
}
