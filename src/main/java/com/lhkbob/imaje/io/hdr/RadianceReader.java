package com.lhkbob.imaje.io.hdr;

import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.data.types.CustomBinaryData;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.ImageFileReader;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.layout.DataLayout;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelFormatBuilder;
import com.lhkbob.imaje.layout.SimpleLayout;
import com.lhkbob.imaje.layout.UnpackedPixelArray;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static com.lhkbob.imaje.io.hdr.RadianceFormat.CONVERSION;

/**
 *
 */
public class RadianceReader implements ImageFileReader {
  private final Data.Factory dataFactory;

  public RadianceReader() {
    this(null);
  }

  public RadianceReader(@Arguments.Nullable Data.Factory factory) {
    if (factory == null) {
      factory = Data.getDefaultDataFactory();
    }
    dataFactory = factory;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Raster<?> read(SeekableByteChannel in) throws IOException {
    ByteBuffer work = IO.createWorkBufferForReading();

    RadianceHeader h = RadianceHeader.read(in, work);

    // Determine imaJe color type based on format variable
    Class<? extends Color> colorType;
    if (h.isFormatRGB()) {
      colorType = RGB.Linear.class;
    } else if (h.isFormatXYZ()) {
      colorType = XYZ.class;
    } else {
      // Unknown and illegal format specification
      throw new InvalidImageException("Unsupported FORMAT: " + h.getFormat());
    }

    // Lookup any applied exposure value that must be undone to get back to the
    // original pixel values.
    double exposure = h.getExposure();

    // Similarly, lookup color correction values per channel, then combine them with the
    // exposure so that the correction logic later on is simpler.
    double[] channelCorrection = h.getColorCorrection();

    channelCorrection[0] *= exposure;
    channelCorrection[1] *= exposure;
    channelCorrection[2] *= exposure;

    // Ignore other known variables like VIEW, SOFTWARE, PIXASPECT, and PRIMARIES
    // FIXME if primaries is provided, should use a color matrix to transform into the default
    // chromaticities assumed for XYZ and RGB.Linear

    // Use a sfloat16 data source, since the file has less precision than that but the loaded image
    // will be directly GPU compatible.
    NumericData<?> data = new CustomBinaryData<>(
        Data.SFLOAT16, dataFactory.newShortData(h.getWidth() * h.getHeight() * 3));
    PixelFormat format = new PixelFormatBuilder().channels(0, 1, 2).types(PixelFormat.Type.SFLOAT)
        .bits(16).build();
    DataLayout layout = new SimpleLayout(h.getWidth(), h.getHeight(), 3);

    UnpackedPixelArray pixelArray = new UnpackedPixelArray(format, layout, data);
    // Read the remainder of the file into the pixel array
    readImage(pixelArray, channelCorrection, h.isTopToBottom(), h.isLeftToRight(), in, work);

    return new Raster(colorType, pixelArray);
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

      // Calculate Y coordinate for imaJe's coordinate system based on resolution specification in
      // file
      int imgY = topToBottom ? (height - y - 1) : y;

      // Now convert the interleaved RGBE byte values into floating point RGB values
      for (int x = 0; x < width; x++) {
        int offset = 4 * x;
        CONVERSION.toNumericValues(Bytes.bytesToIntBE(scan, offset), rgb);

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

      while (x < imgWidth && IO.read(in, work)) {
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
      if (!IO.read(in, work, 4)) {
        throw new InvalidImageException("Unexpected EOF while reading scanline");
      }

      // The read ensures we have at least 4 bytes available
      byte w1 = work.get();
      byte w2 = work.get();
      byte w3 = work.get();
      byte w4 = work.get();
      if (w1 == 2 && w2 == 2 && (w3 & 0x80) == 0) {
        // Validate scan width to ensure it matches the image dimensions:
        // since we know w3's 8th bit is a 0, we don't need to mask it to properly preserve unsigned
        // byte-ness this is not the case for work[3]
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
    while (x < imgWidth && IO.read(in, work, 4)) {
      // Process bytes 4 at a time
      while (x < imgWidth && work.remaining() > 4) {
        int offset = 4 * x;
        work.get(scanlineBuffer, offset, 4);

        // Old way of specifying a run line is to have every mantissa set to 1, in which
        // case the exponent encodes the length of the run and copies the previous pixel value
        if (scanlineBuffer[offset] == 1 && scanlineBuffer[offset + 1] == 1
            && scanlineBuffer[offset + 2] == 1) {
          // Shift the exponent byte left based on how many previous runs have been encountered,
          // each previous run moves this to a higher order byte block (i.e 8 * runs)
          int runLength = (0xff & scanlineBuffer[offset + 3]) << (8 * consecutiveRuns);
          // Copy the previous pixel value in scanlineBuffer to the current offset runLength times
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
}
