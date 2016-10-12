package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public enum Compression {
  NONE(1), RLE(1), ZIPS(1), ZIP(16), PIZ(32), PXR24(16), B44(32), B44A(32);

  private final int linesInBuffer;

  Compression(int linesInBuffer) {
    this.linesInBuffer = linesInBuffer;
  }

  public int getLinesInBuffer() {
    return linesInBuffer;
  }

  public static Compression read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.read(in, work)) {
      throw new InvalidImageException("Unexpected EOF while reading compression enum");
    }
    return values()[work.get()];
  }
}
