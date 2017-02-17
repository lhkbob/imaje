package com.lhkbob.imaje.color.space.spectrum;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.Spectrum;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import java.util.Arrays;
import java.util.Objects;

/**
 * SmitsRGBToSpectrum
 * ==================
 *
 * Implements [Smits '99](http://www.cs.utah.edu/~bes/papers/color/paper.pdf) algorithm for
 * generating a plausible spectrum from an RGB tristimulus. The quality of the conversion is
 * entirely dependent on the accuracy of the spectrum basis functions provided to the constructor.
 *
 * @author Michael Ludwig
 */
public class SmitsRGBToSpectrum<SI extends ColorSpace<RGB<SI>, SI>, SO extends SpectrumSpace<SO>> implements Transform<RGB<SI>, SI, Spectrum<SO>, SO> {
  private final SI rgb;
  private final SO spectrum;

  private final double[] whiteIntegral;
  private final double[] cyanIntegral;
  private final double[] magentaIntegral;
  private final double[] yellowIntegral;
  private final double[] redIntegral;
  private final double[] greenIntegral;
  private final double[] blueIntegral;

  /**
   * Create a new linear RGB to spectrum transformation that resamples and pre-integrates the
   * spectrum basis functions to the dimensionality of the spectral space. If the wavelength
   * boundaries of the spectrum extend beyond the basis functions, those regions of the spectrum are
   * ignored.
   *
   * @param rgbSpace
   *     The linear RGB color space
   * @param spectrumSpace
   *     The spectrum space
   * @param whiteSpectrum
   *     A spectral power distribution corresponding to white
   * @param cyanSpectrum
   *     A spectral power distribution corresponding to cyan
   * @param magentaSpectrum
   *     A spectral power distribution corresponding to magenta
   * @param yellowSpectrum
   *     A spectral power distribution corresponding to yellow
   * @param redSpectrum
   *     A spectral power distribution corresponding to red
   * @param greenSpectrum
   *     A spectral power distribution corresponding to green
   * @param blueSpectrum
   *     A spectral power distribution corresponding to blue
   */
  public SmitsRGBToSpectrum(
      SI rgbSpace, SO spectrumSpace, Curve whiteSpectrum, Curve cyanSpectrum,
      Curve magentaSpectrum, Curve yellowSpectrum, Curve redSpectrum, Curve greenSpectrum,
      Curve blueSpectrum) {
    Arguments.notNull("rgbSpace", rgbSpace);
    Arguments.notNull("spectrumSpace", spectrumSpace);

    Arguments.notNull("whiteSpectrum", whiteSpectrum);
    Arguments.notNull("cyanSpectrum", cyanSpectrum);
    Arguments.notNull("magentaSpectrum", magentaSpectrum);
    Arguments.notNull("yellowSpectrum", yellowSpectrum);
    Arguments.notNull("redSpectrum", redSpectrum);
    Arguments.notNull("greenSpectrum", greenSpectrum);
    Arguments.notNull("blueSpectrum", blueSpectrum);

    spectrum = spectrumSpace;
    rgb = rgbSpace;

    // Resample the spectrum functions into the same number of channels as the spectrum space,
    // and integrate over them within these bins so the actual XYZ conversion is a sum of
    // products.
    whiteIntegral = new double[spectrumSpace.getChannelCount()];
    cyanIntegral = new double[spectrumSpace.getChannelCount()];
    magentaIntegral = new double[spectrumSpace.getChannelCount()];
    yellowIntegral = new double[spectrumSpace.getChannelCount()];
    redIntegral = new double[spectrumSpace.getChannelCount()];
    greenIntegral = new double[spectrumSpace.getChannelCount()];
    blueIntegral = new double[spectrumSpace.getChannelCount()];

    for (int i = 0; i < whiteIntegral.length; i++) {
      double wMin = spectrumSpace.getMinWavelength(i);
      double wMax = spectrumSpace.getMaxWavelength(i);

      whiteIntegral[i] = Functions.integrate(whiteSpectrum, wMin, wMax, 10);
      cyanIntegral[i] = Functions.integrate(cyanSpectrum, wMin, wMax, 10);
      magentaIntegral[i] = Functions.integrate(magentaSpectrum, wMin, wMax, 10);
      yellowIntegral[i] = Functions.integrate(yellowSpectrum, wMin, wMax, 10);
      redIntegral[i] = Functions.integrate(redSpectrum, wMin, wMax, 10);
      greenIntegral[i] = Functions.integrate(greenSpectrum, wMin, wMax, 10);
      blueIntegral[i] = Functions.integrate(blueSpectrum, wMin, wMax, 10);
    }
  }

  @Override
  public Transform<Spectrum<SO>, SO, RGB<SI>, SI> inverse() {
    // Do not support an inverse
    return null;
  }

  @Override
  public SI getInputSpace() {
    return rgb;
  }

  @Override
  public SO getOutputSpace() {
    return spectrum;
  }

  private void addScaledSpectrum(double scale, double[] spectrum, double[] result) {
    for (int i = 0; i < spectrum.length; i++) {
      result[i] += scale * spectrum[i];
    }
  }

  private void computeSpectrum(
      double[] output, double minChannel, double c2, double c3, double[] midSpectrum,
      double[] c2Spectrum, double[] c3Spectrum) {
    addScaledSpectrum(minChannel, whiteIntegral, output);
    if (c2 <= c3) {
      addScaledSpectrum(c2 - minChannel, midSpectrum, output);
      addScaledSpectrum(c3 - c2, c3Spectrum, output);
    } else {
      addScaledSpectrum(c3 - minChannel, midSpectrum, output);
      addScaledSpectrum(c2 - c3, c2Spectrum, output);
    }

    // Clamp to be above 0 and scale
    for (int i = 0; i < output.length; i++) {
      if (output[i] < 0.0) {
        output[i] = 0.0;
      }

      // Scaling value taken from mitsuba/src/libcore/spectrum.cpp
      output[i] *= 0.94;
    }
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", spectrum.getChannelCount(), output.length);

    // Zero out the output array so accumulation isn't incorrect from prior values
    Arrays.fill(output, 0.0);

    double r = input[0];
    double g = input[1];
    double b = input[2];

    // Note that this approximates the channel block as a flat function instead of linearly
    // interpolating it to input[i + 1]
    if (r <= g && r <= b) {
      // Compute spectrum with red as the minimum
      computeSpectrum(output, r, g, b, cyanIntegral, greenIntegral, blueIntegral);
    } else if (g <= r && g <= b) {
      // Compute spectrum with green as the minimum
      computeSpectrum(output, g, r, b, magentaIntegral, redIntegral, blueIntegral);
    } else {
      // Compute spectrum with blue as the minimum
      computeSpectrum(output, b, r, g, yellowIntegral, redIntegral, greenIntegral);
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + rgb.hashCode();
    result = 31 * result + spectrum.hashCode();
    result = 31 * result + Arrays.hashCode(whiteIntegral);
    result = 31 * result + Arrays.hashCode(cyanIntegral);
    result = 31 * result + Arrays.hashCode(magentaIntegral);
    result = 31 * result + Arrays.hashCode(yellowIntegral);
    result = 31 * result + Arrays.hashCode(redIntegral);
    result = 31 * result + Arrays.hashCode(greenIntegral);
    result = 31 * result + Arrays.hashCode(blueIntegral);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof SmitsRGBToSpectrum)) {
      return false;
    }
    SmitsRGBToSpectrum s = (SmitsRGBToSpectrum) o;

    return Objects.equals(s.rgb, rgb) && Objects.equals(s.spectrum, spectrum) && Arrays
        .equals(s.whiteIntegral, whiteIntegral) && Arrays.equals(s.cyanIntegral, cyanIntegral)
        && Arrays.equals(s.magentaIntegral, magentaIntegral) && Arrays
        .equals(s.yellowIntegral, yellowIntegral) && Arrays.equals(s.redIntegral, redIntegral)
        && Arrays.equals(s.greenIntegral, greenIntegral) && Arrays
        .equals(s.blueIntegral, blueIntegral);
  }

  @Override
  public String toString() {
    return String.format("Smits '99 %s -> %s transform", rgb, spectrum);
  }
}
