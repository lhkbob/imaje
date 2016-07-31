package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public interface ImageFileWriter {
  void write(Image<?> image, SeekableByteChannel out) throws IOException;
}
