package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.Image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.List;

/**
 *
 */
public interface ChunkReader {
  ChannelMapping getMapping();

  OpenEXRHeader getHeader();

  // Image instances should be Raster or Mipmap only
  List<? extends Image<?>> getImages();

  void readNextChunk(SeekableByteChannel in, ByteBuffer work) throws IOException;

  OffsetTable readOffsetTable(SeekableByteChannel in, ByteBuffer work) throws IOException;

  void initialize();
}
