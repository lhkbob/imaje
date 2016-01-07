package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class HSVToRGB extends AbstractHueToRGBTransform {
  private final double[] work = new double[3];

  @Override
  public RGBToHSV inverted() {
    return new RGBToHSV();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    work[0] = input[0]; // hue
    work[1] = input[2] * input[1]; // chroma
    work[2] = input[2] - work[1]; // m
    super.transform(work, output);
  }

  @Override
  public int hashCode() {
    return HSVToRGB.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof HSVToRGB;
  }

  @Override
  public String toString() {
    return "HSV -> RGB Transform";
  }
}
