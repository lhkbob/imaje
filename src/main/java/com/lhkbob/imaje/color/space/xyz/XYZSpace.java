package com.lhkbob.imaje.color.space.xyz;

import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.curves.SampledCurve;
import com.lhkbob.imaje.color.transform.curves.UniformlySampledCurve;
import com.lhkbob.imaje.util.Arguments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * XYZSpace
 * ========
 *
 * An abstract space for defining color spaces for {@link XYZ} colors. XYZSpace requires the color
 * space to be defined by three spectral color matching functions, one for each of its primaries.
 * There are two concrete spaces provided: {@link CIE31} and {@link CIE64}; CIE31 is almost always
 * the correct one to use when in doubt.
 *
 * Subclasses must call either {@link #initialize(Curve, Curve, Curve)} or {@link
 * #initializeFromCSV(String)} in their constructor (after calling `super()`) to initialize the
 * color matching function state stored by this class.
 *
 * @author Michael Ludwig
 */
public abstract class XYZSpace<S extends XYZSpace<S>> implements ColorSpace<XYZ<S>, S> {
  // "final" after initialize() called
  private Curve xMatchingFunction;
  private Curve yMatchingFunction;
  private Curve zMatchingFunction;

  /**
   * Initialize this space's X, Y, and Z color matching functions to the three provided curves. The
   * domains of the three curves must match and are assumed to span a reasonable spectral range,
   * e.g. the visible spectrum.
   *
   * @param xMatch
   *     The X color matching function
   * @param yMatch
   *     The Y color matching function
   * @param zMatch
   *     The Z color matching function
   * @throws NullPointerException
   *     if `xMatch`, `yMatch`, or `zMatch` are null
   * @throws IllegalArgumentException
   *     if the domains of the curves do not equal each other
   * @throws IllegalStateException
   *     if the space has already been initialized
   */
  protected void initialize(Curve xMatch, Curve yMatch, Curve zMatch) {
    if (xMatchingFunction != null) {
      throw new IllegalStateException("Already initialized");
    }

    Arguments.equals("domain", xMatch.getDomainMin(), yMatch.getDomainMin());
    Arguments.equals("domain", xMatch.getDomainMin(), zMatch.getDomainMin());
    Arguments.equals("domain", xMatch.getDomainMax(), yMatch.getDomainMax());
    Arguments.equals("domain", xMatch.getDomainMax(), zMatch.getDomainMax());
    xMatchingFunction = xMatch;
    yMatchingFunction = yMatch;
    zMatchingFunction = zMatch;
  }

  /**
   * Initialize this space's X, Y, and Z color matching functions from data contained in a
   * comma-separated file `csvFileName`. This is file should be located in the instance's package in
   * the classpath (i.e. discoverable by `getClass().getResource(csvFileName)`).
   *
   * The CSV file should contain samples of the color matching functions, where each line contains
   * the wavelength of the sample, and then the X, Y, and Z values in that order. The wavelength
   * samples need not be uniformly distributed, but they must be sorted from minimum to maximum.
   *
   * @param csvFileName
   *     The name of a csv file in the class's package
   * @throws IOException
   *     if the file could not be read successfully
   * @throws IllegalStateException
   *     if the space has already had its color matching functions
   *     initialized
   */
  protected void initializeFromCSV(String csvFileName) throws IOException {
    if (xMatchingFunction != null) {
      throw new IllegalStateException("Already initialized");
    }

    URL file = getClass().getResource(csvFileName);
    if (file == null) {
      throw new FileNotFoundException(csvFileName + " not found");
    }

    try {
      List<String> lines = Files.readAllLines(Paths.get(file.toURI()));

      int sampleCount = lines.size();
      if (lines.get(lines.size() - 1).isEmpty()) {
        sampleCount--;
      }
      if (sampleCount < 2) {
        throw new IOException("Expected at least two samples");
      }

      double[] wavelength = new double[sampleCount];
      double[] x = new double[sampleCount];
      double[] y = new double[sampleCount];
      double[] z = new double[sampleCount];

      // First grab first and second samples to get a baseline for wavelength step
      parseCSVLine(lines.get(0), 0, wavelength, x, y, z);
      parseCSVLine(lines.get(1), 1, wavelength, x, y, z);

      boolean uniformWavelength = true;
      double wavelengthStep = wavelength[1] - wavelength[0];
      for (int i = 2; i < sampleCount; i++) {
        parseCSVLine(lines.get(i), i, wavelength, x, y, z);
        if (uniformWavelength
            && Math.abs(wavelength[i] - wavelength[i - 1] - wavelengthStep) > 1e-8) {
          uniformWavelength = false;
        }
      }

      // All lines parsed, use a curve for each depending on whether or not wavelengths were
      // uniformly sampled
      if (uniformWavelength) {
        double min = wavelength[0];
        double max = wavelength[sampleCount - 1];
        xMatchingFunction = new UniformlySampledCurve(min, max, x);
        yMatchingFunction = new UniformlySampledCurve(min, max, y);
        zMatchingFunction = new UniformlySampledCurve(min, max, z);
      } else {
        xMatchingFunction = new SampledCurve(wavelength, x);
        yMatchingFunction = new SampledCurve(wavelength, y);
        zMatchingFunction = new SampledCurve(wavelength, z);
      }
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }

  private static void parseCSVLine(
      String line, int index, double[] wavelength, double[] x, double[] y, double[] z) throws
      IOException {
    String[] parts = line.split(",");
    try {
      if (parts.length == 4) {
        // Wavelength, x, y, and z
        wavelength[index] = Double.parseDouble(parts[0]);
        x[index] = Double.parseDouble(parts[1]);
        y[index] = Double.parseDouble(parts[2]);
        z[index] = Double.parseDouble(parts[3]);
      } else {
        throw new IOException("Unexpected line color matching CSV: " + line);
      }
    } catch (NumberFormatException e) {
      throw new IOException("Could not parse line in CSV", e);
    }
  }

  /**
   * @return The spectral color matching function for the X primary
   */
  public Curve getColorMatchingFunctionX() {
    return xMatchingFunction;
  }

  /**
   * @return The spectral color matching function for the Y primary
   */
  public Curve getColorMatchingFunctionY() {
    return yMatchingFunction;
  }

  /**
   * @return The spectral color matching function for the Z primary
   */
  public Curve getColorMatchingFunctionZ() {
    return zMatchingFunction;
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  @Override
  @SuppressWarnings("unchecked")
  public XYZ<S> newColor() {
    return new XYZ<>((S) this);
  }

  @Override
  public String getChannelName(int channel) {
    switch (channel) {
    case 0:
      return "X";
    case 1:
      return "Y";
    case 2:
      return "Z";
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }
}
