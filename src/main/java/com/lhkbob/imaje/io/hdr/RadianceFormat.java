package com.lhkbob.imaje.io.hdr;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.types.UnsignedSharedExponent;
import com.lhkbob.imaje.io.ImageFileFormat;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * TODO: implement an actual image stream for this file type
 * http://radsite.lbl.gov/radiance/refer/filefmts.pdf
 * https://github.com/NREL/Radiance/blob/combined/src/common/color.c
 */
public class RadianceFormat implements ImageFileFormat {
  private final RadianceReader reader;
  private final RadianceWriter writer;

  public RadianceFormat() {
    this(null);
  }

  public RadianceFormat(@Arguments.Nullable Data.Factory factory) {
    reader = new RadianceReader(factory);
    writer = new RadianceWriter();
  }

  @Override
  public Image<?> read(SeekableByteChannel in) throws IOException {
    return reader.read(in);
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    writer.write(image, out);
  }

  // The masks represent the 4 bytes for RGBE, assuming the bytes were processed as big endian.
  static final UnsignedSharedExponent CONVERSION = new UnsignedSharedExponent(
      0xffL, new long[] { 0xff000000L, 0xff0000L, 0xff00L }, 128);
}
