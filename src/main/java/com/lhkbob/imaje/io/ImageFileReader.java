package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public interface ImageFileReader {
  Image<?> read(SeekableByteChannel in) throws IOException;

  default ImageStream<?> stream(SeekableByteChannel in) throws IOException {
    return ImageStream.ofExisting(read(in));
  }
}
