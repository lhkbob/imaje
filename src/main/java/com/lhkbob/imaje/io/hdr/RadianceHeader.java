package com.lhkbob.imaje.io.hdr;

import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class RadianceHeader {
  private static final byte[][] MAGIC_NUMBERS = new byte[][] {
      new byte[] { '#', '?', 'R', 'G', 'B', 'E' }, // Used by Adobe Photoshop for RGB images
      new byte[] { '#', '?', 'R', 'A', 'D', 'I', 'A', 'N', 'C', 'E' }, // The official magic number
      new byte[] { '#', '?', 'X', 'Y', 'Z', 'E' }
      // Hypothesized magic number for XYZ images from Adobe
  };
  private static final Pattern RESOLUTION_PATTERN = Pattern
      .compile("([-\\+])Y (\\d+) ([-\\+])X (\\d+)");

  public static final String FORMAT_VAR = "FORMAT";
  public static final String EXPOSURE_VAR = "EXPOSURE";
  public static final String COLOR_CORRECTION_VAR = "COLORCORR";
  public static final String SOFTWARE_VAR = "SOFTWARE";
  public static final String PIXEL_ASPECT_VAR = "PIXASPECT";
  public static final String VIEW_VAR = "VIEW";
  public static final String PRIMARIES_VAR = "PRIMARIES";

  public static final String FORMAT_RGB_VALUE = "32-bit_rle_rgbe";
  public static final String FORMAT_XYZ_VALUE = "32-bit_rle_xyze";

  // A non-standard variable name used to encode resolution in map returned by processVariables()
  private static final String RESOLUTION_VAR = "RESOLUTION";

  private final Map<String, String> variables;
  private int width;
  private int height;
  private boolean leftToRight;
  private boolean topToBottom;

  public RadianceHeader() {
    variables = new HashMap<>();
  }

  private RadianceHeader(Map<String, String> variables) {
    this.variables = variables;
  }

  public String getVariable(String key) {
    return variables.get(key);
  }

  public void setVariable(String key, String value) {
    variables.put(key, value);
  }

  public double getExposure() throws InvalidImageException {
    String exp = getVariable(EXPOSURE_VAR);
    if (exp == null)
      return 1.0;
    else {
      try {
        return Double.parseDouble(exp);
      } catch(NumberFormatException e) {
        throw new InvalidImageException("Unable to parse EXPOSURE variable", e);
      }
    }
  }

  public void setExposure(double exposure) {
    setVariable(EXPOSURE_VAR, String.format("%.4f", exposure));
  }

  public double[] getColorCorrection() throws InvalidImageException {
    String colorCorrValue = getVariable(COLOR_CORRECTION_VAR);
    if (colorCorrValue == null) {
      return new double[] { 1.0, 1.0, 1.0 };
    }

    String[] channels = colorCorrValue.split("\\s+");
    if (channels.length != 3) {
      throw new InvalidImageException(
          "Expected 3 correction values for COLORCORR variable, not: " + channels.length);
    }

    try {
      double[] channelCorrection = new double[3];
      channelCorrection[0] *= Double.parseDouble(channels[0]);
      channelCorrection[1] *= Double.parseDouble(channels[1]);
      channelCorrection[2] *= Double.parseDouble(channels[2]);
      return channelCorrection;
    } catch (NumberFormatException e) {
      throw new InvalidImageException("Unable to parse COLORCORR variable", e);
    }
  }

  public void setColorCorrection(double r, double g, double b) {
    setVariable(COLOR_CORRECTION_VAR, String.format("%.4f %.4f %.4f", r, g, b));
  }

  public String getFormat() {
    return getVariable(FORMAT_VAR);
  }

  public boolean isFormatRGB() {
    return FORMAT_RGB_VALUE.equals(getFormat());
  }

  public boolean isFormatXYZ() {
    return FORMAT_XYZ_VALUE.equals(getFormat());
  }

  public void setFormat(String format) {
    setVariable(FORMAT_VAR, format);
  }

  public void setFormatRGB() {
    setFormat(FORMAT_RGB_VALUE);
  }

  public void setFormatXYZ() {
    setFormat(FORMAT_XYZ_VALUE);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public boolean isTopToBottom() {
    return topToBottom;
  }

  public boolean isLeftToRight() {
    return leftToRight;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setTopToBottom(boolean topToBottom) {
    this.topToBottom = topToBottom;
  }

  public void setLeftToRight(boolean leftToRight) {
    this.leftToRight = leftToRight;
  }

  public static RadianceHeader read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // Validate magic number at the beginning of the file
    checkMagicNumber(in, work);
    // Read variable header and resolution string before image data is read
    Map<String, String> vars = processVariables(in, work);

    // Remove RESOLUTION variable and put remaining variables into header
    String resVar = vars.remove(RESOLUTION_VAR);
    RadianceHeader h = new RadianceHeader(vars);

    // Parse resVar and decode its parts into explicit variables of h
    Matcher m = RESOLUTION_PATTERN.matcher(vars.get("RESOLUTION"));
    m.matches(); // This is known to be true since that's how processVariables() terminates
    h.height = Integer.parseInt(m.group(2));
    h.topToBottom = "-".equals(m.group(1));
    h.width = Integer.parseInt(m.group(4));
    h.leftToRight = "+".equals(m.group(3));

    return h;
  }

  public void write(SeekableByteChannel out, ByteBuffer work) throws IOException {
    // Build header text
    StringBuilder header = new StringBuilder();
    // Magic number
    header.append("#?RADIANCE\n");
    // Comment
    header.append("#Written by imaJe\n");
    // Write all variables
    for (Map.Entry<String, String> v: variables.entrySet()) {
      header.append(v.getKey()).append('=').append(v.getValue()).append('\n');
    }

    // Now format the resolution string
    if (topToBottom)
      header.append('-');
    else
      header.append('+');
    header.append(' ').append(height);

    if (leftToRight)
      header.append('+');
    else
      header.append('-');
    header.append(' ').append(width).append('\n');

    // Write header bytes as ASCII
    work.put(header.toString().getBytes("ASCII")).flip();
    IO.write(work, out);
  }

  private static void checkMagicNumber(SeekableByteChannel in, ByteBuffer work) throws IOException {
    int count = 0;
    boolean[] matches = new boolean[MAGIC_NUMBERS.length];
    Arrays.fill(matches, true);

    while (IO.read(in, work)) {
      while (work.hasRemaining()) {
        byte toCheck = work.get();

        // Check byte against all sequences that still match
        boolean stillMatching = false;
        for (int i = 0; i < matches.length; i++) {
          if (!matches[i]) {
            continue;
          }

          // There is no need to check length of the MAGIC_NUMBERS[i] because we return the moment
          // we complete a sequence. If we can't complete the sequence but have processed more bytes
          // than it contains, it means there must have been a mismatched byte earlier in the sequence
          // and so the first if -> continues; check guards against it.
          if (toCheck != MAGIC_NUMBERS[i][count]) {
            // Wrong byte value for this sequence, so it cannot match anymore.
            matches[i] = false;
          } else if (count == MAGIC_NUMBERS[i].length - 1) {
            // Completed the magic number, so terminate - this is valid assuming that none of the
            // accepted magic numbers are prefixes of one another (currently true).
            return;
          } else {
            // The sequence remains valid
            stillMatching = true;
          }
        }

        if (stillMatching) {
          count++;
        } else {
          throw new InvalidImageException("Radiance magic number not found");
        }
      }
    }

    // If the end of the file was reached before completing the magic number, that is also a failure
    throw new InvalidImageException("Radiance magic number not found");
  }

  private static Map<String, String> processVariables(SeekableByteChannel in, ByteBuffer work) throws
      IOException {
    Map<String, String> vars = new HashMap<>();
    StringBuilder sb = new StringBuilder();
    while (IO.read(in, work)) {
      // Process every read byte by appending it to the string builder
      while (work.hasRemaining()) {
        int b = (0xff & work.get());
        if (b == '\n') {
          // move onto next variable
          String line = sb.toString().trim();
          if (!line.isEmpty()) {
            // check for variable pattern
            int equals = line.indexOf('=');
            if (equals >= 0) {
              vars.put(line.substring(0, equals), line.substring(equals + 1));
            } else if (RESOLUTION_PATTERN.matcher(line).matches()) {
              // Return resolution specification in special variable
              vars.put(RESOLUTION_VAR, line);
              return vars;
            } else if (!line.startsWith("#")) {
              throw new InvalidImageException("Unexpected header line: " + line);
            }
          }
          // empty line at end of header, but we just skip it since we process the dimensions
          // as part of this method, too

          // regardless, reset buffer
          sb.setLength(0);
        } else {
          sb.append((char) b);
        }
      }
    }

    throw new InvalidImageException("Did not encounter resolution specification");
  }
}
