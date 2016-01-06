package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public class HLSToRGBTransform extends AbstractHueToRGBTransform {
  private final double[] work = new double[3];

  @Override
  public RGBToHLSTransform inverted() {
    return new RGBToHLSTransform();
  }

  @Override
  public void transform(double[] input, double[] output) {
    checkVectorDimensions(input, output);

    work[0] = input[0]; // hue
    work[1] = (1.0 - Math.abs(2.0 * input[1] - 1.0)) * input[2]; // chroma
    work[2] = input[1] - 0.5 * work[1]; // m

    super.transform(work, output);
  }

  @Override
  public int hashCode() {
    return HLSToRGBTransform.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof HLSToRGBTransform;
  }

  @Override
  public String toString() {
    return "HLS -> RGB Transform";
  }
}
