package com.lhkbob.imaje.io.hdr;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Transforms;
import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.ImageFileWriter;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static com.lhkbob.imaje.io.hdr.RadianceFormat.CONVERSION;

/**
 *
 */
public class RadianceWriter implements ImageFileWriter {
  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    if (!(image instanceof Raster)) {
      throw new InvalidImageException("Only 2D Raster images are supported");
    }

    writeRaster((Raster<?>) image, out);
  }

  private <T extends Color> void writeRaster(Raster<T> image, SeekableByteChannel out) throws
      IOException {
    ByteBuffer work = IO.createWorkBufferForWriting();

    RadianceHeader h = new RadianceHeader();
    // Format, while we could choose to write XYZE as well, RGBE seems to be more universally
    // supported and since a conversion has to happen might as well take it to RGB
    h.setFormatRGB();
    // Exposure, which we default to 1.0 -> in the future it might be worthwhile to find an exposure
    // that minimizes data loss when the unexposed pixel values are encoded as 4 bytes.
    h.setExposure(1.0);
    // FIXME we could try and include color correction and/or primaries if we know that T is
    // a particular type of RGB space, etc.

    h.write(out, work);

    T color = Color.newInstance(image.getColorType());
    ColorTransform<T, RGB.Linear> toLinear = Transforms.newTransform(image.getColorType(), RGB.Linear.class);
    // FIXME implement some RLE encoding for images of appropriate size
    for (int y = image.getHeight() - 1; y >= 0; y--) {
      for (int x = 0; x < image.getWidth(); x++) {
        image.get(x, y, color); // Ignore alpha since Radiance can't store that

        // Convert to linear RGB
        RGB.Linear toWrite = toLinear.apply(color);

        // Encode RGB as 4 bytes
        Bytes.intToBytesBE((int) CONVERSION.toBits(toWrite.getChannels()), work);

        // Push pixel data to channel if we've reached the end
        if (work.remaining() < 4) {
          IO.write(work, out);
        }
      }
    }

    // Flush out any last row of pixel data
    if (work.hasRemaining()) {
      IO.write(work, out);
    }
  }
}
