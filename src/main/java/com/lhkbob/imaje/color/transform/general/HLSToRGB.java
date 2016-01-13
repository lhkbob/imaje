package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class HLSToRGB extends AbstractHueToRGBTransform {
  private final double[] work = new double[3];

  @Override
  public boolean equals(Object o) {
    return o instanceof HLSToRGB;
  }

  @Override
  public HLSToRGB getLocallySafeInstance() {
    // HLSToRGB uses a member variable for work during the transform() call so a new instance
    // must be created for it to be used safely.
    return new HLSToRGB();
  }

  @Override
  public int hashCode() {
    return HLSToRGB.class.hashCode();
  }

  @Override
  public RGBToHLS inverted() {
    return new RGBToHLS();
  }

  @Override
  public String toString() {
    return "HLS -> RGB Transform";
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    work[0] = input[0]; // hue
    work[1] = (1.0 - Math.abs(2.0 * input[1] - 1.0)) * input[2]; // chroma
    work[2] = input[1] - 0.5 * work[1]; // m

    super.transform(work, output);
  }
}
