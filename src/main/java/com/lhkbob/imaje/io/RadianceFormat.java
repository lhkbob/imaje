package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Transforms;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.data.types.CustomBinaryData;
import com.lhkbob.imaje.data.types.UnsignedSharedExponent;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.DataLayout;
import com.lhkbob.imaje.layout.SimpleLayout;
import com.lhkbob.imaje.layout.UnpackedPixelArray;
import com.lhkbob.imaje.util.ByteOrderUtils;
import com.lhkbob.imaje.util.IOUtils;
import com.lhkbob.imaje.util.PixelFormatBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: implement an actual image stream for this file type
 * http://radsite.lbl.gov/radiance/refer/filefmts.pdf
 * https://github.com/NREL/Radiance/blob/combined/src/common/color.c
 */
public class RadianceFormat implements ImageFileFormat {
  private final Data.Factory dataFactory;

  public RadianceFormat() {
    this(null);
  }

  public RadianceFormat(Data.Factory factory) {
    if (factory == null) {
      factory = Data.getDefaultDataFactory();
    }
    dataFactory = factory;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Raster<?> read(SeekableByteChannel in) throws IOException {
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(WORK_BUFFER_LEN);
    // Configure buffer limit to be ready for getContent()
    work.limit(0);

    // Validate magic number at the beginning of the file
    checkMagicNumber(in, work);
    // Read variable header and resolution string before image data is read
    Map<String, String> vars = processVariables(in, work);

    // Determine imaJe color type based on format line
    Class<? extends Color> colorType;
    if ("32-bit_rle_rgbe".equals(vars.get("FORMAT"))) {
      colorType = RGB.Linear.class;
    } else if ("32-bit_rle_xyze".equals(vars.get("FORMAT"))) {
      colorType = XYZ.class;
    } else {
      // Unknown and illegal format specification
      throw new InvalidImageException("Unsupported FORMAT: " + vars.get("FORMAT"));
    }

    // Lookup any applied exposure value that must be undone to get back to the
    // original pixel values.
    double exposure = 1.0;
    if (vars.containsKey("EXPOSURE")) {
      try {
        exposure = Double.parseDouble(vars.get("EXPOSURE"));
      } catch (NumberFormatException e) {
        throw new InvalidImageException("Unable to parse EXPOSURE variable", e);
      }
    }
    // Similarly, lookup color correction values per channel, but combine them with the
    // exposure so that the correction logic later on is simpler.
    double[] channelCorrection = new double[] { exposure, exposure, exposure };
    if (vars.containsKey("COLORCORR")) {
      String[] channels = vars.get("COLORCORR").split("\\s+");
      if (channels.length != 3) {
        throw new InvalidImageException(
            "Expected 3 correction values for COLORCORR variable, not: " + channels.length);
      }

      try {
        channelCorrection[0] *= Double.parseDouble(channels[0]);
        channelCorrection[1] *= Double.parseDouble(channels[1]);
        channelCorrection[2] *= Double.parseDouble(channels[2]);
      } catch (NumberFormatException e) {
        throw new InvalidImageException("Unable to parse COLORCORR variable", e);
      }
    }

    // Ignore other known variables like VIEW, SOFTWARE, PIXASPECT, and PRIMARIES
    // FIXME if primaries is provided, should use a color matrix to transform into the default
    // chromaticities assumed for XYZ and RGB.Linear

    // Now process resolution line
    Matcher m = RESOLUTION_PATTERN.matcher(vars.get("RESOLUTION"));
    m.matches(); // This is known to be true since that's how processVariables() terminates
    int height = Integer.parseInt(m.group(2));
    boolean topToBottom = m.group(1).equals("-");
    int width = Integer.parseInt(m.group(4));
    boolean leftToRight = m.group(3).equals("+");

    // Use a sfloat16 data source, since the file has less precision than that but the loaded image
    // will be directly GPU compatible.
    NumericData<?> data = new CustomBinaryData<>(
        Data.SFLOAT16, dataFactory.newShortData(width * height * 3));
    PixelFormat format = new PixelFormatBuilder().channels(0, 1, 2).types(PixelFormat.Type.SFLOAT)
        .bits(16).build();
    DataLayout layout = new SimpleLayout(width, height, 3);

    UnpackedPixelArray pixelArray = new UnpackedPixelArray(format, layout, data, 0L);
    // Read the remainder of the file into the pixel array
    readImage(pixelArray, channelCorrection, topToBottom, leftToRight, in, work);

    return new Raster(colorType, pixelArray);
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    if (!(image instanceof Raster)) {
      throw new InvalidImageException("Only 2D Raster images are supported");
    }

    writeRaster((Raster<?>) image, out);
  }

  private <T extends Color> void writeRaster(Raster<T> image, SeekableByteChannel out) throws IOException {
    ColorTransform<T, RGB.Linear> toLinear = Transforms.newTransform(image.getColorType(), RGB.Linear.class);
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(WORK_BUFFER_LEN);

    // Build header text
    StringBuilder header = new StringBuilder();
    // Magic number
    header.append("#?RADIANCE\n");
    // Comment
    header.append("#Written by imaJe\n");
    // Format, while we could choose to write XYZE as well, RGBE seems to be more universally supported
    // and since a conversion has to happen might as well take it to RGB
    header.append("FORMAT=32-bit_rle_rgbe\n");
    // Exposure, which we default to 1.0 -> in the future it might be worthwhile to find an exposure
    // that minimizes data loss when the unexposed pixel values are encoded as 4 bytes.
    header.append("EXPOSURE=1.0\n");
    // FIXME we could try and include color correction and/or primaries if we know that T is
    // a particular type of RGB space, etc.

    // Set resolution and use the common -Y height +X width layout even if the data is likely
    // in the +Y +X format. Other libraries may not be as flexible at consuming other layouts.
    header.append("-Y ").append(image.getHeight()).append(" +X ").append(image.getWidth()).append("\n");


    // Write header bytes as ASCII
    work.put(header.toString().getBytes("ASCII")).flip();
    IOUtils.write(work, out);

    T color = Color.newInstance(image.getColorType());
    // FIXME implement some RLE encoding for images of appropriate size
    for (int y = image.getHeight() - 1; y >= 0; y--) {
      for (int x = 0; x < image.getWidth(); x++) {
        image.get(x, y, color); // Ignore alpha since Radiance can't store that

        // Convert to linear RGB
        RGB.Linear toWrite = toLinear.apply(color);

        // Encode RGB as 4 bytes
        ByteOrderUtils.intToBytesBE((int) CONVERSION.toBits(toWrite.getChannels()), work);

        // Push pixel data to channel if we've reached the end
        if (work.remaining() < 4) {
          IOUtils.write(work, out);
        }
      }
    }

    // Flush out any last row of pixel data
    if (work.hasRemaining()) {
      IOUtils.write(work, out);
    }
  }

  private void checkMagicNumber(SeekableByteChannel in, ByteBuffer work) throws IOException {
    int count = 0;
    boolean[] matches = new boolean[MAGIC_NUMBERS.length];
    Arrays.fill(matches, true);

    while(IOUtils.read(in, work)) {
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
          throw new UnsupportedImageFormatException("Radiance magic number not found");
        }
      }
    }

    // If the end of the file was reached before completing the magic number, that is also a failure
    throw new UnsupportedImageFormatException("Radiance magic number not found");
  }

  private Map<String, String> processVariables(SeekableByteChannel in, ByteBuffer work) throws
      IOException {
    Map<String, String> vars = new HashMap<>();
    StringBuilder sb = new StringBuilder();
    while(IOUtils.read(in, work)) {
      // Process every read byte by appending it to the string builder
      while (work.hasRemaining()) {
        byte b = work.get();
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
              vars.put("RESOLUTION", line);
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

  private void readImage(
      UnpackedPixelArray image, double[] channelCorrection, boolean topToBottom,
      boolean leftToRight, SeekableByteChannel in, ByteBuffer work) throws IOException {
    int width = image.getLayout().getWidth();
    int height = image.getLayout().getHeight();

    // While the actual image is just 3 channels, the byte data is R, G, B, E
    byte[] scan = new byte[width * 4];

    double[] rgb = new double[3];
    for (int y = 0; y < height; y++) {
      // First read/unpack the next scanline
      readScanLine(width, in, work, scan);

      // Calculate Y coordinate for imaJe's coordinate system based on resolution specification in file
      int imgY = topToBottom ? (height - y - 1) : y;

      // Now convert the interleaved RGBE byte values into floating point RGB values
      for (int x = 0; x < width; x++) {
        int offset = 4 * x;
        CONVERSION.toNumericValues(ByteOrderUtils.bytesToIntBE(scan, offset), rgb);

        // Apply channel corrections to undo modifications to the written pixel values
        for (int i = 0; i < 3; i++) {
          rgb[i] /= channelCorrection[i];
        }

        // Calculate X coordinate for imaJe's coordinate system based on resolution specification
        int imgX = leftToRight ? x : (width - x - 1);

        // Push numeric RGB values into the pixel array data
        image.getData().setValues(3 * (imgY * width + imgX), rgb);
      }
    }
  }

  private void readRLEScanLine(
      int imgWidth, SeekableByteChannel in, ByteBuffer work, byte[] scanlineBuffer) throws
      IOException {
    // Scanline is adaptively RLE'ed in separate channels: R, G, B, and then E. So expand
    // and reorder the run lengths into scanlineBuffer
    for (int i = 0; i < 4; i++) {
      int x = 0; // Logical progress through scanline
      int remaining = 0; // Remaining bytes in scan block to read
      int repeat = 1;

      while (x < imgWidth && IOUtils.read(in, work)) {
        while (x < imgWidth && work.hasRemaining()) {
          if (remaining == 0) {
            // The next byte specifies the type of run block
            int blockLength = (0xff & work.get());
            if (blockLength > 128) {
              // Runlength block, so next byte is repeated length - 128 times
              remaining = 1;
              repeat = blockLength - 128;
            } else {
              // Plain block, so read length bytes with no repeat
              remaining = blockLength;
              repeat = 1;
            }
          } else {
            // Block content
            byte b = work.get();
            // Copy b repeat times into the scanline buffer
            if (x + repeat > imgWidth) {
              throw new InvalidImageException("RLE block exceeds scanline width");
            }
            for (int j = 0; j < repeat; j++) {
              scanlineBuffer[4 * x + i] = b;
              x++;
            }

            remaining--;
          }
        }
      }

      if (x != imgWidth) {
        throw new InvalidImageException("RLE scanline did not provide entire image row before EOF");
      }
    }
  }

  private void readScanLine(
      int imgWidth, SeekableByteChannel in, ByteBuffer work, byte[] scanlineBuffer) throws
      IOException {
    // Special case for small or extra large images that can't use RLE, which doesn't require
    // reading any bytes
    if (imgWidth >= 8 && imgWidth <= 0x7fff) {
      // Must read 4 bytes to detect if the scanline is run length encoded
      if (!IOUtils.read(in, work, 4)) {
        throw new InvalidImageException("Unexpected EOF while reading scanline");
      }

      // The read ensures we have at least 4 bytes available
      byte w1 = work.get();
      byte w2 = work.get();
      byte w3 = work.get();
      byte w4 = work.get();
      if (w1 == 2 && w2 == 2 && (w3 & 0x80) == 0) {
        // Validate scan width to ensure it matches the image dimensions:
        // since we know w3's 8th bit is a 0, we don't need to mask it to properly preserve unsigned byte-ness
        // this is not the case for work[3]
        int scanWidth = (w3 << 8) | (0xff & w4);
        if (scanWidth != imgWidth) {
          throw new InvalidImageException(
              "Scanline width was " + scanWidth + " but expected " + imgWidth);
        }

        readRLEScanLine(imgWidth, in, work, scanlineBuffer);
        return;
      } else {
        // Move position back 4 elements to include the previously peeked at words
        work.position(work.position() - 4);
      }
    }

    // Otherwise read a simple scanline
    readSimpleScanLine(imgWidth, in, work, scanlineBuffer);
  }

  private void readSimpleScanLine(
      int imgWidth, SeekableByteChannel in, ByteBuffer work, byte[] scanlineBuffer) throws
      IOException {
    // The scanline is either a sequence of interleaved 4 RGBE bytes or postfixed specified
    // RLE-encoded data. Consecutive RLE packets indicate LE-ordered larger run lengths.
    int x = 0;
    int consecutiveRuns = 0;
    while(x < imgWidth && IOUtils.read(in, work, 4)) {
      // Process bytes 4 at a time
      while (x < imgWidth && work.remaining() > 4) {
        int offset = 4 * x;
        work.get(scanlineBuffer, offset, 4);

        // Old way of specifying a run line is to have every mantissa set to 1, in which
        // case the exponent encodes the length of the run and copies the previous pixel value
        if (scanlineBuffer[offset] == 1 && scanlineBuffer[offset + 1] == 1 && scanlineBuffer[offset + 2] == 1) {
          // Shift the exponent byte left based on how many previous runs have been encountered,
          // each previous run moves this to a higher order byte block (i.e 8 * runs)
          int runLength = (0xff & scanlineBuffer[offset + 3]) << (8 * consecutiveRuns);
          // Copy the previous pixel value in scanlineBuffr to the current offset runLength times
          for (int i = 0; i < runLength; i++) {
            System.arraycopy(scanlineBuffer, offset - 4, scanlineBuffer, offset + runLength * 4, 4);
          }
          consecutiveRuns++;
          x += runLength;
        } else {
          // Raw pixel value, already copied into the scanline, just clear the run counter
          consecutiveRuns = 0;
          x++;
        }
      }
    }

    // Check if the entire scanline was read before EOF was reached
    if (x != imgWidth) {
      throw new InvalidImageException("Full scanline was not provided before EOF");
    }
  }

  // The masks represent the 4 bytes for RGBE, assuming the bytes were processed as big endian.
  private static final UnsignedSharedExponent CONVERSION = new UnsignedSharedExponent(
      0xffL, new long[] { 0xff000000L, 0xff0000L, 0xff00L }, 128);
  private static final byte[][] MAGIC_NUMBERS = new byte[][] {
      new byte[] { '#', '?', 'R', 'G', 'B', 'E' }, // Used by Adobe Photoshop for RGB images
      new byte[] { '#', '?', 'R', 'A', 'D', 'I', 'A', 'N', 'C', 'E' }, // The official magic number
      new byte[] { '#', '?', 'X', 'Y', 'Z', 'E' }
      // Hypothesized magic number for XYZ images from Adobe
  };
  private static final Pattern RESOLUTION_PATTERN = Pattern
      .compile("([-\\+])Y (\\d+) ([-\\+])X (\\d+)");
  private static final int WORK_BUFFER_LEN = 4096;
}
