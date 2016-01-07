package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class HLSToRGB extends AbstractHueToRGBTransform {
  private final double[] work = new double[3];

  @Override
  public RGBToHLS inverted() {
    return new RGBToHLS();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    work[0] = input[0]; // hue
    work[1] = (1.0 - Math.abs(2.0 * input[1] - 1.0)) * input[2]; // chroma
    work[2] = input[1] - 0.5 * work[1]; // m

    super.transform(work, output);
  }

  @Override
  public int hashCode() {
    return HLSToRGB.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof HLSToRGB;
  }

  @Override
  public String toString() {
    return "HLS -> RGB Transform";
  }
}
