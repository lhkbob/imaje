package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public enum LineOrder {
  INCREASING_Y, DECREASING_Y, RANDOM_Y;

  public static LineOrder read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.read(in, work)) {
      throw new InvalidImageException("Unexpected EOF while reading line order enum");
    }
    return values()[work.get()];
  }
}
