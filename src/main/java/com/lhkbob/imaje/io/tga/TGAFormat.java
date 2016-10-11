package com.lhkbob.imaje.io.tga;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.io.ImageFileFormat;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * See: http://www.paulbourke.net/dataformats/tga/
 * http://www.fileformat.info/format/tga/egff.htm#TGA-DMYID.2
 * http://www.dca.fee.unicamp.br/~martino/disciplinas/ea978/tgaffs.pdf
 */
public class TGAFormat implements ImageFileFormat {
  private final TGAReader reader;
  private final TGAWriter writer;

  public TGAFormat() {
    this(null);
  }

  public TGAFormat(@Arguments.Nullable Data.Factory factory) {
    reader = new TGAReader(factory);
    writer = new TGAWriter();
  }

  @Override
  public Image<?> read(SeekableByteChannel in) throws IOException {
    return reader.read(in);
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    writer.write(image, out);
  }

  static void putUnsignedByte(ByteBuffer work, int value) {
    work.put((byte) (0xff & value));
  }

  static void putUnsignedShort(ByteBuffer work, int value) {
    Bytes.shortToBytesBE((short) (0xffff & value), work);
  }

  static int getUnsignedByte(ByteBuffer work) {
    return 0xff & work.get();
  }

  static int getUnsignedShort(ByteBuffer work) {
    return 0xffff & Bytes.bytesToShortBE(work);
  }
}
