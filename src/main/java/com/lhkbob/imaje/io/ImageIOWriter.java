package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.util.BufferedImageConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

/**
 */
public class ImageIOWriter implements ImageFileWriter {
  private final String formatSuffix;

  public ImageIOWriter(String formatSuffix) {
    this.formatSuffix = formatSuffix;

  }

  private javax.imageio.ImageWriter getWriter() throws UnsupportedImageFormatException {
    Iterator<javax.imageio.ImageWriter> writers = ImageIO.getImageWritersBySuffix(formatSuffix);
    if (!writers.hasNext()) {
      throw new UnsupportedImageFormatException("Unavailable or unknown image format file suffix: " + formatSuffix);
    }
    return writers.next();
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    if (!(image instanceof Raster)) {
      throw new InvalidImageException("Only 2D Raster images are supported for ImageIO writing");
    }

    OutputStream ioWrapper = Channels.newOutputStream(out);
    try (ImageOutputStream stream = ImageIO.createImageOutputStream(ioWrapper)) {
      javax.imageio.ImageWriter writer = getWriter();
      writer.setOutput(stream);
      writer.write(BufferedImageConverter.wrapOrConvert((Raster<?>) image));
    }
  }
}
