package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.io.ImageFileFormat;
import com.lhkbob.imaje.io.ImageStream;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public abstract class ImageIOFormat implements ImageFileFormat {
  private final ImageIOReader reader;
  private final ImageIOWriter writer;

  public ImageIOFormat(String formatSuffix, @Arguments.Nullable Data.Factory factory) {
    reader = new ImageIOReader(formatSuffix, factory);
    writer = new ImageIOWriter(formatSuffix);
  }

  @Override
  public Raster<?> read(SeekableByteChannel in) throws IOException {
    return reader.read(in);
  }

  @Override
  public ImageStream<?> stream(SeekableByteChannel in) throws IOException {
    return reader.stream(in);
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    writer.write(image, out);
  }
}
