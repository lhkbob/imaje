package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public class ScanLineOffsetTable implements OffsetTable {
  // line buffer offsets ordered by increasing y
  private final long[] offsets;
  private final OpenEXRHeader header;

  public ScanLineOffsetTable(OpenEXRHeader header, long[] offsets) {
    this.header = header;
    this.offsets = offsets;
  }

  public int getLineFromY(int y) {
    double dy = y - header.getDataWindow().getMinY();
    return Functions.floorInt(dy / header.getCompression().getLinesInBuffer());
  }

  public long getLineOffset(int line) {
    return offsets[line];
  }

  @Override
  public int getTotalOffsets() {
    return offsets.length;
  }

  @Override
  public long getOffset(int... chunkCoords) {
    Arguments.equals("chunkCoords.length", 1, chunkCoords.length);
    return getLineOffset(chunkCoords[0]);
  }

  public static ScanLineOffsetTable read(
      OpenEXRHeader header, SeekableByteChannel in, ByteBuffer work) throws IOException {
    int linesInBuffer = header.getCompression().getLinesInBuffer();
    int size = Functions.ceilInt(header.getDataWindow().height() / (double) linesInBuffer);

    // confirm chunkCount is correct
    if (header.getFormat() != null) {
      // openexr 2.0 format, so type must be specified
      if (header.getChunkCount() != size) {
        throw new InvalidImageException(
            "Calculated scanline offset table doesn't match reported chunk count: " + size + " vs "
                + header.getChunkCount());
      }
    }

    byte[] data = new byte[size * 8];
    if (!IO.fill(data, in, work)) {
      throw new InvalidImageException("Unable to load full scanline offset table");
    }
    long[] offsets = new long[size];
    for (int i = 0; i < size; i++) {
      offsets[i] = Bytes.bytesToLongLE(data, i * 8);
    }

    return new ScanLineOffsetTable(header, offsets);
  }
}
