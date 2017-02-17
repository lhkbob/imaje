package com.lhkbob.imaje.color.space.spectrum;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.Spectrum;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.space.rgb.CustomRGBSpace;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.color.transform.ExplicitInverse;
import com.lhkbob.imaje.color.transform.Illuminants;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.curves.UniformlySampledCurve;
import com.lhkbob.imaje.util.Arguments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * SpectrumSpace
 * =============
 *
 * SpectrumSpace is the parent ColorSpace definition for use with the {@link Spectrum} color. The
 * spectrum space defines a minimum and maximum wavelength and then uniformly samples the spectrum
 * within that range (or uniformly samples it in log-space). The number of samples made is equal to
 * the space's channel count, which is configurable per instance.
 *
 * The spectrum space discretizes the continuous spectral space by splitting the wavelength bounds
 * of the space into a number of blocks based on the channel count. The channel value stored in
 * a {@link Spectrum} instead corresponds to the value of the spectrum at the lower wavelength
 * associated with that channel block. Values are approximated by linear interpolation across
 * the channel block when arbitrary wavelengths are requested.
 *
 * All wavelength values are measured and reported in nanometers.
 *
 * @author Michael Ludwig
 */
public abstract class SpectrumSpace<S extends SpectrumSpace<S>> implements ColorSpace<Spectrum<S>, S> {
  private final int channelCount;
  private final double minWavelength;
  private final double maxWavelength;
  private final boolean logSampleWavelength;

  private final double step;

  private ExplicitInverse<S, Spectrum<S>, CIE31, XYZ<CIE31>> toXYZ;

  /**
   * Create a new SpectrumSpace with the given `channelCount`, `minWavelength`, and `maxWavelength`.
   * If `logSampleWavelength` is true then the space uniformly samples the wavelength range in
   * log-space before converting back to actual wavelengths. If it is false, the space uniformly
   * samples linear wavelengths. The channel count must be at least one. The min wavelength
   * represents the lower bound of the first channel. The max wavelength represents
   * the upper bound of the last channel, not the lower bound of that channel.
   *
   * It is recommended that subclasses hide the wavelength and log-sampling parameters so that a
   * concrete subclass represents a specific spectrum range, and only the resolution is configurable.
   *
   * @param channelCount
   *     The number of samples of the spectrum
   * @param minWavelength
   *     The lower bound of the spectrum
   * @param maxWavelength
   *     The upper bound of the spectrum
   * @param logSampleWavelength
   *     True if the samples are distributed logarithmically over the range
   * @throws IllegalArgumentException
   *     if `channelCount` is less than 1, or if `minWavelength` is less than or equal to 0, or if
   *     `maxWavelength` is not greater than `minWavelength`
   */
  @SuppressWarnings("unchecked")
  public SpectrumSpace(
      int channelCount, double minWavelength, double maxWavelength, boolean logSampleWavelength) {
    Arguments.isPositive("channelCount", channelCount);
    Arguments.isPositive("minWavelength", 0.0);
    Arguments.isGreaterThan("maxWavelength", minWavelength, maxWavelength);

    this.channelCount = channelCount;
    if (logSampleWavelength) {
      // Store the min/max boundaries in log space for efficiency
      this.minWavelength = Math.log(minWavelength);
      this.maxWavelength = Math.log(maxWavelength);
    } else {
      // Store the min/max boundaries linearly
      this.minWavelength = minWavelength;
      this.maxWavelength = maxWavelength;
    }
    this.logSampleWavelength = logSampleWavelength;

    step = (maxWavelength - minWavelength) / channelCount;
  }

  /**
   * Complete initialization of this space by providing the intermediate RGB space and spectrum
   * basis functions used to build a SmitsRGBToSpectrum color transform as the explicit inverse of
   * the spectrum to XYZ transform.
   *
   * @param rgbSpace
   *     The intermediate linear RGB color space the basis functions are related to
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
   * @param <R>
   *     The RGB space defining primaries
   * @throws IllegalStateException
   *     if called more than once
   */
  @SuppressWarnings("unchecked")
  protected <R extends ColorSpace<RGB<R>, R>> void initialize(
      R rgbSpace, Curve whiteSpectrum, Curve cyanSpectrum, Curve magentaSpectrum,
      Curve yellowSpectrum, Curve redSpectrum, Curve greenSpectrum, Curve blueSpectrum) {
    if (toXYZ != null) {
      throw new IllegalStateException("initialize() can only be called once");
    }

    SpectrumToXYZ<S, CIE31> toXYZ = new SpectrumToXYZ<>((S) this, CIE31.SPACE);
    SmitsRGBToSpectrum<R, S> rgbToSpectrum = new SmitsRGBToSpectrum<>(rgbSpace, (S) this,
        whiteSpectrum, cyanSpectrum, magentaSpectrum, yellowSpectrum, redSpectrum, greenSpectrum,
        blueSpectrum);
    Transform<XYZ<CIE31>, CIE31, Spectrum<S>, S> fromXYZ = new Composition<>(
        rgbSpace.getXYZTransform().inverse(), rgbToSpectrum);

    this.toXYZ = new ExplicitInverse<>(toXYZ, fromXYZ);
  }

  /**
   * Complete initialization by reading the spectrum basis functions defined in
   * `resources/com.lhkbob.color.space.spectrum`, which use a linear RGB space with the same
   * primaries as sRGB and an equal energy whitepoint.
   *
   * @throws IllegalStateException
   *     if called more than once
   */
  protected void initializeDefault() {
    try {
      // Don't use getClass() since the default files are defined relative to SpectrumSpace
      // and not some arbitrary subclass.

      Curve white = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie31_white.txt");
      Curve cyan = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie31_cyan.txt");
      Curve magenta = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie31_magenta.txt");
      Curve yellow = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie31_yellow.txt");
      Curve red = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie31_red.txt");
      Curve green = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie31_green.txt");
      Curve blue = readSmitsSpectrumBasis(SpectrumSpace.class, "smits_rgb2spec_cie31_blue.txt");

      CustomRGBSpace<CIE31> dfltRGB = new CustomRGBSpace<>(
          Illuminants.newE(1.0).toXYZ(), Yxy.newCIE31(0.64, 0.33), Yxy.newCIE31(0.3, 0.6),
          Yxy.newCIE31(0.15, 0.06), null);

      initialize(dfltRGB, white, cyan, magenta, yellow, red, green, blue);
    } catch (IOException e) {
      throw new RuntimeException("Unable to load default rgb2spec files", e);
    }
  }

  /**
   * Convenience wrapper about {@link #readSmitsSpectrumBasis(URI)} that generates a URI
   * from a call `owner`'s {@link Class#getResource(String)}.
   *
   * @param owner The class that the basis resource is relative to
   * @param name The name of the resource
   * @return A curve that samples the basis data from the resource
   * @throws IOException if there is a parsing error, file I/O error, or the resource can't be found
   */
  public static Curve readSmitsSpectrumBasis(Class<?> owner, String name) throws IOException {
    URL file = owner.getResource(name);
    if (file == null) {
      throw new FileNotFoundException(name + " resource not found for " + owner);
    }

    try {
      return readSmitsSpectrumBasis(file.toURI());
    } catch(URISyntaxException e) {
      throw new IOException(e);
    }
  }

  /**
   * Read a text file at `uri` and return a {@link Curve} representation. The file is assumed
   * to have a spectral power value per line. The values are uniformly sampled over the wavelength
   * range of 360 to 800, in that order.
   *
   * @param uri The location of the spectrum basis file
   * @return A curve that samples the basis data from the file
   * @throws IOException if there is a parsing error or other file I/O error
   */
  public static Curve readSmitsSpectrumBasis(URI uri) throws IOException {
    try {
      List<String> lines = Files.readAllLines(Paths.get(uri));

      int sampleCount = lines.size();
      if (lines.get(lines.size() - 1).isEmpty()) {
        sampleCount--;
      }
      if (sampleCount < 2) {
        throw new IOException("Expected at least two samples");
      }

      double[] spd = new double[sampleCount];
      for (int i = 0; i < sampleCount; i++) {
        spd[i] = Double.parseDouble(lines.get(i));
      }

      // The domain min and max are specified in the README for the curve text files
      return new UniformlySampledCurve(360.0, 800.0, spd);
    } catch (NumberFormatException e) {
      throw new IOException(e);
    }
  }

  /**
   * @return True if wavelength bounds over the channel samples are distributed logarithmically.
   */
  public boolean isWavelengthLogSampled() {
    return logSampleWavelength;
  }

  /**
   * @return The lower bound of the spectral range of this space
   */
  public double getMinWavelength() {
    if (logSampleWavelength) {
      return Math.exp(minWavelength);
    } else {
      return minWavelength;
    }
  }

  /**
   * @return The upper bound of the spectral range of this space
   */
  public double getMaxWavelength() {
    if (logSampleWavelength) {
      return Math.exp(maxWavelength);
    } else {
      return maxWavelength;
    }
  }

  /**
   * Get the minimum wavelength of the given channel sample, where a channel represents a block
   * of wavelengths from the returned wavelength to {@link #getMaxWavelength(int)}.
   *
   * @param sample
   *     The channel index
   * @return The lower wavelength bound for a given channel sample
   *
   * @throws IndexOutOfBoundsException
   *     if `sample` is less than 0 or greater than or equal to channel count
   */
  public double getMinWavelength(int sample) {
    Arguments.checkIndex("sample", channelCount, sample);

    double wl = sample * step + minWavelength;
    if (logSampleWavelength) {
      return Math.exp(wl);
    } else {
      return wl;
    }
  }

  /**
   * Get the maximum wavelength of the given channel sample, where a channel represents a block
   * of wavelengths from {@link #getMinWavelength(int)} to the returned wavelength.
   *
   * @param sample
   *     The channel index
   * @return The upper wavelength bound for a given channel sample
   *
   * @throws IndexOutOfBoundsException
   *     if `sample` is less than 0 or greater than or equal to channel count
   */
  public double getMaxWavelength(int sample) {
    Arguments.checkIndex("sample", channelCount, sample);

    double wl = (sample + 1) * step + minWavelength;
    if (logSampleWavelength) {
      return Math.exp(wl);
    } else {
      return wl;
    }
  }

  /**
   * Get the channel index that contains the given `wavelength`. The actual integer index
   * is found by flooring the returned value. The fractional portion of the returned value
   * represents the fraction between the minimum and maximum bounds of the channel block that
   * contains `wavelength`.
   *
   * If the wavelength is outside of the overall minimum and maximum of the space, then -1 is
   * returned.
   *
   * @param wavelength
   *     The wavelength to lookup
   * @return The channel index combined with the interpolation fraction, or -1 if the wavelength
   * is out of bounds
   */
  public double getChannel(double wavelength) {
    if (logSampleWavelength) {
      wavelength = Math.log(wavelength);
    }

    if (wavelength < minWavelength || wavelength > maxWavelength) {
      // Outside of spectrum range
      return -1.0;
    }

    return (wavelength - minWavelength) / step;
  }

  @Override
  public int getChannelCount() {
    return channelCount;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Spectrum<S> newColor() {
    return new Spectrum<>((S) this);
  }

  @Override
  public Transform<Spectrum<S>, S, XYZ<CIE31>, CIE31> getXYZTransform() {
    return toXYZ;
  }

  @Override
  public String getChannelName(int channel) {
    double min = getMinWavelength(channel);
    double max = getMaxWavelength(channel);
    return String.format("lambda %.2f-%.2f (%d)", min, max, channel);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!getClass().isInstance(o)) {
      return false;
    }
    SpectrumSpace s = (SpectrumSpace) o;
    return channelCount == s.channelCount && Double.compare(minWavelength, s.minWavelength) == 0
        && Double.compare(maxWavelength, s.maxWavelength) == 0
        && logSampleWavelength == s.logSampleWavelength;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Double.hashCode(minWavelength);
    result = 31 * result + Double.hashCode(maxWavelength);
    result = 31 * result + Integer.hashCode(channelCount);
    result = 31 * result + Boolean.hashCode(logSampleWavelength);
    return result;
  }

  @Override
  public String toString() {
    return String.format("Spectrum(%d, %.2f to %.2f)", channelCount, minWavelength, maxWavelength);
  }
}
