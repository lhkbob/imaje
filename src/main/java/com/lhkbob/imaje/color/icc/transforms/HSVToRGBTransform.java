package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public class HSVToRGBTransform extends AbstractHueToRGBTransform {
  private final double[] work = new double[3];

  @Override
  public RGBToHSVTransform inverted() {
    return new RGBToHSVTransform();
  }

  @Override
  public void transform(double[] input, double[] output) {
    checkVectorDimensions(input, output);

    work[0] = input[0]; // hue
    work[1] = input[2] * input[1]; // chroma
    work[2] = input[2] - work[1]; // m
    super.transform(work, output);
  }

  @Override
  public int hashCode() {
    return HSVToRGBTransform.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof HSVToRGBTransform;
  }

  @Override
  public String toString() {
    return "HSV -> RGB Transform";
  }
}
