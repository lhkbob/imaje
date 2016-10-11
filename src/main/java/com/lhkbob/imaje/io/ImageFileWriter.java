package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public interface ImageFileWriter {
  // FIXME clarify what types of transformations are allowed to be done to make the image savable.
  // I think it should be allowed to change color space, and convert to a single raster;
  // converting to a single raster should have a specific policy:
  // 1. option one is layout all levels, layers, etc. into a 2D grid so data is not lost.
  // 2. take base level or first layer, etc. which is simpler, but lossier, but also more consistent
  //    with in-code casting from an array to a raster would do.
  void write(Image<?> image, SeekableByteChannel out) throws IOException;
}
