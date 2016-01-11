package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class RGBToYCbCr implements Transform {
  private final double kr;
  private final double kb;

  private final double umax;
  private final double vmax;

  public RGBToYCbCr(double kb, double kr, double umax, double vmax) {
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
  public YCbCrToRGB inverted() {
    return new YCbCrToRGB(kb, kr, umax, vmax);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    // Y from R, G, and B
    output[0] = kr * input[0] + (1.0 - kr - kb) * input[1] + kb * input[2];
    // Cb from Y and B
    output[1] = umax * (input[2] - output[0]) / (1.0 - kb);
    // Cr from Y and R
    output[2] = vmax * (input[0] - output[0]) / (1.0 - kr);
  }

  @Override
  public RGBToYCbCr getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
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
    if (!(o instanceof RGBToYCbCr)) {
      return false;
    }
    RGBToYCbCr c = (RGBToYCbCr) o;
    return Double.compare(c.kr, kr) == 0 && Double.compare(c.kb, kb) == 0
        && Double.compare(c.umax, umax) == 0 && Double.compare(c.vmax, vmax) == 0;
  }

  @Override
  public String toString() {
    return String
        .format("RGB -> Yb*r* (kb: %.3f, kr: %.3f, b-max: %.3f, r-max: %.3f)", kb, kr, umax, vmax);
  }
}
