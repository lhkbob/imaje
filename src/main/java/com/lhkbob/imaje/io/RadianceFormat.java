package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.data.types.CustomBinaryData;
import com.lhkbob.imaje.data.types.UnsignedSharedExponent;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelLayout;
import com.lhkbob.imaje.layout.RasterLayout;
import com.lhkbob.imaje.layout.UnpackedPixelArray;
import com.lhkbob.imaje.util.ByteOrderUtils;
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
 * TODO: implement file writing as well
 */
public class RadianceFormat implements ImageFileReader {
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

    // Validate magic number at the beginning of the file
    checkMagicNumber(in, work);
    // Read variable header and resolution string before image data is read
    Map<String, String> vars = processVariables(in, work);

    // Determine imaJe color type based on format line
    Class<? extends Color> colorType;
    if ("32-bit-rle-rgbe".equals(vars.get("FORMAT"))) {
      colorType = RGB.Linear.class;
    } else if ("32-bit-rle-xyze".equals(vars.get("FORMAT"))) {
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
    PixelLayout layout = new RasterLayout(width, height, 3);

    UnpackedPixelArray pixelArray = new UnpackedPixelArray(format, layout, data, 0L);
    // Read the remainder of the file into the pixel array
    readImage(pixelArray, channelCorrection, topToBottom, leftToRight, in, work);

    return new Raster(colorType, pixelArray);
  }

  private void checkMagicNumber(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // Magic number must be '#?RGBE' or '#?RADIANCE'
    int count = 0;
    boolean[] matches = new boolean[MAGIC_NUMBERS.length];
    Arrays.fill(matches, true);

    while (work.position() > 0 || in.read(work) >= 0) {
      work.flip();
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
            // Completed the match, so compact the buffer so any additional data is available for
            // the next section of the reader.
            work.compact();
            return;
          } else {
            // The sequence remains valid
            stillMatching = true;
          }
        }

        if (!stillMatching) {
          throw new UnsupportedImageFormatException("Radiance magic number not found");
        }
      }

      // Clear for the next read, there are no more remaining valid bytes in work
      work.clear();
    }

    // If the end of the file was reached before completing the magic number, that is also a failure
    throw new UnsupportedImageFormatException("Radiance magic number not found");
  }

  private Map<String, String> processVariables(SeekableByteChannel in, ByteBuffer work) throws
      IOException {
    Map<String, String> vars = new HashMap<>();
    StringBuilder sb = new StringBuilder();
    while (work.position() > 0 || in.read(work) >= 0) {
      // Continue reading until the end of file, this will break out once resolution variable
      // has been specified, which is the signal that the variable preface of the file is finished.
      work.flip();
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
              work.compact();
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

      // Read everything from the work buffer, so reset it for the next read
      work.clear();
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
        byte r = scan[4 * (y * width + x)];
        byte g = scan[4 * (y * width + x) + 1];
        byte b = scan[4 * (y * width + x) + 2];
        byte e = scan[4 * (y * width + x) + 3];
        CONVERSION.toNumericValues(ByteOrderUtils.bytesToIntBE(r, g, b, e), rgb);

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
      while (x < imgWidth && (work.position() > 0 || in.read(work) >= 0)) {
        work.flip();
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

        if (work.hasRemaining()) {
          work.compact();
        } else {
          work.clear();
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
      while (work.position() < 4) {
        if (in.read(work) < 0) {
          throw new InvalidImageException("Unable to read scanline byte marker before end-of-file");
        }
      }

      // Use absolute gets so we don't mess up the position in case the scanline is not RLE
      // and the first bytes must be included as actual pixel data.
      if (work.get(0) == 2 && work.get(1) == 2 && (work.get(2) & 0x80) == 0) {
        // Validate scan width to ensure it matches the image dimensions:
        // since we know work[2]'s 8th bit is a 0, we don't need to mask it to properly preserve unsigned byte-ness
        // this is not the case for work[3]
        int scanWidth = (work.get(2) << 8) | (0xff & work.get(3));
        if (scanWidth != imgWidth) {
          throw new InvalidImageException(
              "Scanline width was " + scanWidth + " but expected " + imgWidth);
        }

        // Discard the 4 bytes that controlled the scanline specification
        work.flip().position(4);
        work.compact();
        readRLEScanLine(imgWidth, in, work, scanlineBuffer);
        return;
      }
    }

    // Otherwise read a simple scanline
    readSimpleScanLine(imgWidth, in, work, scanlineBuffer);
  }

  private void readSimpleScanLine(
      int imgWidth, SeekableByteChannel in, ByteBuffer work, byte[] scanlineBuffer) throws
      IOException {
    // The scanline is just a sequence of 4 * imgWidth bytes with interleaved RGBE values.
    int x = 0;
    while (x < imgWidth && (work.position() >= 4 || in.read(work) >= 0)) {
      work.flip(); // Make ready for reading

      // Process bytes 4 at a time
      int pixels = Math.min(work.remaining() / 4, imgWidth - x);
      work.get(scanlineBuffer, 4 * x, pixels * 4);
      x += pixels;

      if (work.hasRemaining()) {
        work.compact();
      } else {
        work.clear();
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
  private static final int WORK_BUFFER_LEN = 2048;
}
