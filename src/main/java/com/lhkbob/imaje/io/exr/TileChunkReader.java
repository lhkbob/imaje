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
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class TileChunkReader extends AbstractChunkReader {
  public TileChunkReader(
      Data.Factory dataFactory, ChannelMapping mapping) {
    super(dataFactory, mapping);
  }

  @Override
  public void readNextChunk(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // Read tile coordinates (4 integers)
    if (!IO.read(in, work, 16)) {
      throw new InvalidImageException("Unable to read part tile coordinates");
    }
    int dx = Bytes.bytesToIntLE(work);
    int dy = Bytes.bytesToIntLE(work);
    int lx = Bytes.bytesToIntLE(work);
    int ly = Bytes.bytesToIntLE(work);

    // Read in data size of the chunk
    if (!IO.read(in, work, 4)) {
      throw new InvalidImageException("Unable to read tile part data size");
    }
    int dataSize = Bytes.bytesToIntLE(work);

    Box2Int dataWindow = getHeader().getDataWindow();
    TileDescription tiles = getHeader().getTileDescription();

    // Calculate one dimensional mipmap level from the ripmap coordinates
    int level = -1;
    if (lx != ly) {
      // Off diagonal, but clamped to the smaller dimension's level is a valid continuation
      // of the mipmap chain for non-square images.
      int numX = tiles.getLevelCountX(dataWindow);
      int numY = tiles.getLevelCountY(dataWindow);
      if (numX > numY) {
        // There are more levels along the X axis, so a level coordinate where ly == numY - 1 is
        // valid and the mipmap level is equal to lx
        if (ly == numY - 1) {
          level = lx;
        }
      } else {
        // As above but its valid when lx == numX - 1 and then level = ly
        if (lx == numX - 1) {
          level = ly;
        }
      }
    } else {
      // Ripmap along the diagonal means the mipmap level is equal to either lx or ly
      level = lx;
    }

    // For RIPMAP chunks, its possible for non diagonal chunks to be provided in the file but there
    // is no in-memory storage allocated for those so they should just be skipped over.
    if (level < 0) {
      if (!IO.skip(in, work, dataSize)) {
        throw new InvalidImageException("Unable to fully skip non-diagonal RIPMAP chunk");
      }
      return;
    }

    // Determine window for this tile
    Box2Int chunkWindow = tiles.getTileDataWindow(dataWindow, dx, dy, lx, ly);
    readChunk(
        dataSize, chunkWindow, getDataForLevel(level), getHeader().getLayoutForMipmap(level), in,
        work);
  }

  @Override
  public OffsetTable readOffsetTable(SeekableByteChannel in, ByteBuffer work) throws IOException {
    return TileOffsetTable.read(getHeader(), in, work);
  }

  @Override
  protected int getMaxUncompressedDataSize() {
    // The chunk block that is read holds an entire tile
    return getHeader().getTileDescription().getWidth() * getHeader().getTileDescription()
        .getHeight() * getHeader().getBytesPerPixel();
  }

  @Override
  protected List<NumericData<?>> createBackingData() {
    Box2Int dataWindow = getHeader().getDataWindow();
    TileDescription tiles = getHeader().getTileDescription();

    if (getHeader().getTileDescription().getLevelMode() == LevelMode.ONE_LEVEL) {
      // Simply create one base level
      return Collections.singletonList(createData(dataWindow.width() * dataWindow.height()));
    } else {
      int numX = tiles.getLevelCountX(dataWindow);
      int numY = tiles.getLevelCountY(dataWindow);
      int numLevels = Math.max(numX, numY);
      List<NumericData<?>> mipData = new ArrayList<>(numLevels);

      for (int i = 0; i < numLevels; i++) {
        int lx = Math.min(numX - 1, i);
        int ly = Math.min(numY - 1, i);

        Box2Int levelWindow = tiles.getLevelDataWindow(dataWindow, lx, ly);
        mipData.add(createData(levelWindow.width() * levelWindow.height()));
      }

      return mipData;
    }
  }
}
