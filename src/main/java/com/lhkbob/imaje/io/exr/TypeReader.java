package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@FunctionalInterface
interface TypeReader<T> {
  T read(SeekableByteChannel in, ByteBuffer work) throws IOException;

  static <T> List<T> readAll(
      SeekableByteChannel in, ByteBuffer work, TypeReader<T> reader) throws IOException {
    List<T> values = new ArrayList<>();

    // The IO.read() is not pushed into the while loop condition so that it can be an exception
    // condition since the list is defined to be terminated by a null byte.
    if (!IO.read(in, work)) {
      throw new InvalidImageException("Unexpected EOF while reading type list");
    }
    while (work.get() != 0) {
      // reset back to the byte we just read since it is actually part of the channel definition
      work.position(work.position() - 1);
      values.add(reader.read(in, work));

      if (!IO.read(in, work)) {
        throw new InvalidImageException("Unexpected EOF while reading type list");
      }
    }
    // we're done when we reach a null byte, no need to reset the stream
    return values;
  }

  static String readNullTerminatedString(SeekableByteChannel in, ByteBuffer work) throws
      IOException {
    StringBuilder sb = new StringBuilder();
    while (IO.read(in, work)) {
      while (work.hasRemaining()) {
        int b = (0xff & work.get());
        if (b != 0) {
          sb.append((char) b);
        } else {
          return sb.toString();
        }
      }
    }

    throw new InvalidImageException("Unexpected EOF before null character string terminator");
  }
}
