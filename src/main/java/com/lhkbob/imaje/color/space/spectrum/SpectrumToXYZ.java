package com.lhkbob.imaje.color.space.spectrum;

import com.lhkbob.imaje.color.Spectrum;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.xyz.XYZSpace;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import java.util.Arrays;
import java.util.Objects;

/**
 * SpectrumToXYZ
 * =============
 *
 * Color transform from a {@link Spectrum} to {@link XYZ}. This relies on the color matching
 * functions reported by the XYZ space. See
 * [here](https://en.wikipedia.org/wiki/CIE_1931_color_space#Color_matching_functions) for more
 * details, although the theory extends to any XYZ space and not just CIE 1931.
 *
 * @author Michael Ludwig
 */
public class SpectrumToXYZ<SI extends SpectrumSpace<SI>, SO extends XYZSpace<SO>> implements Transform<Spectrum<SI>, SI, XYZ<SO>, SO> {
  private final SI spectrum;
  private final SO xyz;

  private final double[] xIntegral;
  private final double[] yIntegral;
  private final double[] zIntegral;
  private final double normalization;

  /**
   * Create a new spectrum to XYZ transformation that resamples and pre-integrates the x, y, and z
   * color matching functions of the XYZ space to the dimensionality of the spectral space. If the
   * wavelength boundaries of the spectrum extend beyond the color matching function, those regions
   * of the spectrum are ignored.
   *
   * @param spectrumSpace
   *     The spectrum space
   * @param xyzSpace
   *     The XYZ color space
   */
  public SpectrumToXYZ(SI spectrumSpace, SO xyzSpace) {
    Arguments.notNull("spectrumSpace", spectrumSpace);
    Arguments.notNull("xyzSpace", xyzSpace);

    spectrum = spectrumSpace;
    xyz = xyzSpace;

    // Resample the color matching functions into the same number of channels as the spectrum,
    // and integrate over the CMF within these bins so the actual XYZ conversion is a sum of
    // products.
    xIntegral = new double[spectrumSpace.getChannelCount()];
    yIntegral = new double[spectrumSpace.getChannelCount()];
    zIntegral = new double[spectrumSpace.getChannelCount()];

    double yInt = 0.0;
    for (int i = 0; i < xIntegral.length; i++) {
      double wMin = spectrumSpace.getMinWavelength(i);
      double wMax = spectrumSpace.getMaxWavelength(i);

      xIntegral[i] = Functions.integrate(xyzSpace.getColorMatchingFunctionX(), wMin, wMax, 10);
      yIntegral[i] = Functions.integrate(xyzSpace.getColorMatchingFunctionY(), wMin, wMax, 10);
      zIntegral[i] = Functions.integrate(xyzSpace.getColorMatchingFunctionZ(), wMin, wMax, 10);

      yInt += yIntegral[i];
    }

    normalization = 1.0 / yInt;
  }

  @Override
  public Transform<XYZ<SO>, SO, Spectrum<SI>, SI> inverse() {
    return null;
  }

  @Override
  public SI getInputSpace() {
    return spectrum;
  }

  @Override
  public SO getOutputSpace() {
    return xyz;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", spectrum.getChannelCount(), input.length);
    Arguments.equals("output.length", 3, output.length);

    // Zero out the output array so accumulation isn't incorrect from prior values
    Arrays.fill(output, 0.0);

    // Note that this approximates the channel block as a flat function instead of linearly
    // interpolating it to input[i + 1]
    for (int i = 0; i < xIntegral.length; i++) {
      output[0] = input[i] * xIntegral[i];
      output[1] = input[i] * yIntegral[i];
      output[2] = input[i] * zIntegral[i];
    }

    output[0] *= normalization;
    output[1] *= normalization;
    output[2] *= normalization;
    return true;
  }

  @Override
  public int hashCode() {
    int result = SpectrumToXYZ.class.hashCode();
    result = 31 * result + spectrum.hashCode();
    result = 31 * result + xyz.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof SpectrumToXYZ)) {
      return false;
    }
    SpectrumToXYZ t = (SpectrumToXYZ) o;
    return Objects.equals(t.xyz, xyz) && Objects.equals(t.spectrum, spectrum);
  }

  @Override
  public String toString() {
    return String.format("%s -> %s transform", spectrum, xyz);
  }
}
