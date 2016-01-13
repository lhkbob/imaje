package com.lhkbob.imaje.color.transform.general;

/**
 * YCbCr's range is [0, 1], [-0.5, 0.5], [-0.5, 0.5]
 */
public class YCbCrToRGB implements Transform {
  private final double kb;
  private final double kr;
  private final double umax;
  private final double vmax;

  public YCbCrToRGB(double kb, double kr, double umax, double vmax) {
    this.kr = kr;
    this.kb = kb;
    this.umax = umax;
    this.vmax = vmax;
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
  public int getInputChannels() {
    return 3;
  }

  @Override
  public YCbCrToRGB getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
  }

  @Override
  public int getOutputChannels() {
    return 3;
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
  public RGBToYCbCr inverted() {
    return new RGBToYCbCr(kb, kr, umax, vmax);
  }

  @Override
  public String toString() {
    return String
        .format("Yb*r* -> RGB (kb: %.3f, kr: %.3f, b-max: %.3f, r-max: %.3f)", kb, kr, umax, vmax);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    // R from Y and Cr
    output[0] = (1.0 - kr) * input[0] * input[2] / vmax;
    // B from Y and Cb
    output[2] = (1.0 - kb) * input[0] * input[1] / vmax;
    // G from Y, R, and B
    output[1] = (input[0] - kr * output[0] - kb * output[2]) / (1.0 - kr - kb);
  }
}
