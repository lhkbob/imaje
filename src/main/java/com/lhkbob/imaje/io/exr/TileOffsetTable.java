/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public class TileOffsetTable implements OffsetTable {
  private final int totalTiles;

  // offsets for each tile/level in the image
  private final int levelYStride;
  private final int levelXStride;
  private final long[][][] offsets; // 3-dimensional (lx * ly, dy, dx)

  public TileOffsetTable(OpenEXRHeader header, long[][][] offsets) {
    this.offsets = offsets;

    switch (header.getTileDescription().getLevelMode()) {
    case ONE_LEVEL:
      levelXStride = 0;
      levelYStride = 0;
      break;
    case MIPMAP_LEVELS:
      levelXStride = 1;
      levelYStride = 0;
      break;
    case RIPMAP_LEVELS:
      levelXStride = 1;
      levelYStride = header.getTileDescription().getLevelCountX(header.getDataWindow());
      break;
    default:
      throw new RuntimeException("SHOULD NOT HAPPEN");
    }

    int total = 0;
    for (long[][] offsetForLevel : offsets) {
      total += getTotalLevelOffsets(offsetForLevel);
    }
    totalTiles = total;
  }

  @Override
  public long getOffset(int... chunkCoords) {
    if (chunkCoords.length == 3) {
      // Assume single level is provided
      return getTileOffset(chunkCoords[0], chunkCoords[1], chunkCoords[2]);
    } else if (chunkCoords.length == 4) {
      return getTileOffset(chunkCoords[0], chunkCoords[1], chunkCoords[2], chunkCoords[3]);
    } else {
      throw new IllegalArgumentException(
          "Must provide 3 or 4 chunk coordinates, not " + chunkCoords.length);
    }
  }

  @Override
  public int getTotalOffsets() {
    return totalTiles;
  }

  public long getTileOffset(int dx, int dy, int l) {
    return getTileOffset(dx, dy, l, l);
  }

  public long getTileOffset(int dx, int dy, int lx, int ly) {
    return offsets[lx * levelXStride + ly * levelYStride][dy][dx];
  }

  public static TileOffsetTable read(
      OpenEXRHeader header, SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (header.getTileDescription() == null) {
      throw new InvalidImageException("No tile description provided for tiled image");
    }

    int size = header.getTileDescription().getOffsetTableSize(header.getDataWindow());
    // confirm chunkCount is correct
    if (header.getFormat() != null) {
      // openexr 2.0 format, so type must be specified
      if (header.getChunkCount() != size) {
        throw new InvalidImageException(
            "Calculated tile offset table doesn't match reported chunk count: " + size + " vs "
                + header.getChunkCount());
      }
    }

    int[][] levelCounts = header.getTileDescription().getTileCounts(header.getDataWindow());
    int numX = levelCounts[0].length;
    int numY = levelCounts[1].length;

    byte[] data = new byte[size * 8];
    if (!IO.fill(data, in, work)) {
      throw new InvalidImageException("Unable to load tile offset table completely");
    }

    int offset = 0;
    long[][][] offsets;
    switch (header.getTileDescription().getLevelMode()) {
    case ONE_LEVEL:
    case MIPMAP_LEVELS:
      offsets = new long[numX][][];
      for (int l = 0; l < offsets.length; l++) {
        offsets[l] = parseLevelOffsets(l, levelCounts[0], levelCounts[1], data, offset);
        offset += 8 * getTotalLevelOffsets(offsets[l]);
      }
      break;
    case RIPMAP_LEVELS:
      offsets = new long[numX * numY][][];
      for (int ly = 0; ly < numY; ly++) {
        for (int lx = 0; lx < numX; lx++) {
          int l = ly * numX + lx;
          offsets[l] = parseLevelOffsets(l, levelCounts[0], levelCounts[1], data, offset);
          offset += 8 * getTotalLevelOffsets(offsets[l]);
        }
      }
      break;
    default:
      throw new UnsupportedImageFormatException(
          "Unknown level mode: " + header.getTileDescription().getLevelMode());
    }

    return new TileOffsetTable(header, offsets);
  }

  private static int getTotalLevelOffsets(long[][] offsets) {
    int count = 0;
    for (long[] offset : offsets) {
      count += offset.length;
    }
    return count;
  }

  private static long[][] parseLevelOffsets(
      int level, int[] xLevelCounts, int[] yLevelCounts, byte[] data, int offset) {
    long[][] offsets = new long[yLevelCounts[level]][];
    for (int dy = 0; dy < offsets.length; dy++) {
      offsets[dy] = new long[xLevelCounts[level]];
      for (int dx = 0; dx < offsets[dy].length; dx++) {
        offsets[dy][dx] = Bytes.bytesToLongLE(data, offset);
        offset += 8;
      }
    }

    return offsets;
  }
}
