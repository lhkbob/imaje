package com.lhkbob.imaje.color.transform.general;

/**
 *
 */
public class RGBToHLS extends AbstractRGBToHueTransform {
  private static final double EPS = 1e-8;

  @Override
  public HLSToRGB inverted() {
    return new HLSToRGB();
  }

  @Override
  public void transform(double[] input, double[] output) {
    // Super class stores hue, and min, max components into output
    super.transform(input, output);

    double hue = output[0];
    double c = output[2] - output[1];
    double saturation;
    double lightness = 0.5 * (output[2] + output[1]);
    if (c < EPS) {
      // Neutral color, use hue = 0 arbitrarily
      hue = 0.0;
      saturation = 0.0;
    } else {
      hue *= 60.0; // Scale hue to 0 to 360 degrees
      saturation = c / (1.0 - Math.abs(2.0 * lightness - 1.0));
    }

    output[0] = hue;
    output[1] = lightness;
    output[2] = saturation;
  }

  @Override
  public int hashCode() {
    return RGBToHLS.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof RGBToHLS;
  }

  @Override
  public String toString() {
    return "RGB -> HLS Transform";
  }
}
