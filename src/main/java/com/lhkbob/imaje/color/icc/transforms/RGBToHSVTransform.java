package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public class RGBToHSVTransform extends AbstractRGBToHueTransform {
  private static final double EPS = 1e-8;

  @Override
  public HSVToRGBTransform inverted() {
    return new HSVToRGBTransform();
  }

  @Override
  public void transform(double[] input, double[] output) {
    // Super class stores hue, and min, max components into output
    super.transform(input, output);

    double hue = output[0];
    double c = output[2] - output[1];
    double saturation;
    double value = output[2];
    if (c < EPS) {
      // Neutral color, use hue = 0 arbitrarily
      hue = 0.0;
      saturation = 0.0;
    } else {
      hue *= 60.0; // Scale hue to 0 to 360 degrees
      saturation = c / value;
    }

    output[0] = hue;
    output[1] = saturation;
    output[2] = value;
  }

  @Override
  public int hashCode() {
    return RGBToHSVTransform.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof RGBToHSVTransform;
  }

  @Override
  public String toString() {
    return "RGB -> HSV Transform";
  }
}
