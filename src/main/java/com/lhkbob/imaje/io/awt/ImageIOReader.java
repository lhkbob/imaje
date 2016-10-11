package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.io.ImageFileReader;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 */
public class ImageIOReader implements ImageFileReader {
  private final String formatSuffix;
  private final Data.Factory factory;

  public ImageIOReader(String formatSuffix) {
    this(formatSuffix, null);
  }

  public ImageIOReader(String formatSuffix, Data.Factory factory) {
    if (factory == null) {
      factory = Data.getDefaultDataFactory();
    }
    this.formatSuffix = formatSuffix;
    this.factory = factory;
  }

  private ImageReader getReader() throws UnsupportedImageFormatException {
    Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(formatSuffix);
    if (!readers.hasNext()) {
      throw new UnsupportedImageFormatException(
          "Unavailable or unknown image format file suffix: " + formatSuffix);
    }
    return readers.next();
  }

  @Override
  public Raster<?> read(SeekableByteChannel in) throws IOException {
    // ImageIO uses plain Java IO as its interface, so unwrap NIO type
    InputStream ioWrapper = Channels.newInputStream(in);
    try (ImageInputStream stream = ImageIO.createImageInputStream(ioWrapper)) {
      ImageReader reader = getReader();
      reader.setInput(stream);
      BufferedImage img = reader.read(0);
      if (img == null) {
        throw new InvalidImageException("Cannot read image using ImageIO libraries");
      }
      return BufferedImageConverter.wrapOrConvert(img, factory);
    }
  }
}
