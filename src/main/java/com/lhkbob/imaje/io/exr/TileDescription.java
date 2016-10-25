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
import com.lhkbob.imaje.util.Functions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public class TileDescription {
  private final int xSize, ySize; // unsigned ints
  private final LevelMode levelMode;
  private final RoundingMode roundingMode;

  public TileDescription(int xSize, int ySize, LevelMode levelMode, RoundingMode roundingMode) {
    this.xSize = xSize;
    this.ySize = ySize;

    this.levelMode = levelMode;
    this.roundingMode = roundingMode;
  }

  public int getWidth() {
    return xSize;
  }

  public int getHeight() {
    return ySize;
  }

  public LevelMode getLevelMode() {
    return levelMode;
  }

  public RoundingMode getRoundingMode() {
    return roundingMode;
  }

  public static TileDescription read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.read(in, work, 9)) {
      throw new InvalidImageException("Unexpected EOF while reading tile description");
    }

    int xSize = Bytes.bytesToIntLE(work);
    int ySize = Bytes.bytesToIntLE(work);

    // Dimensions are unsigned integers, so check if they overflow java's signed integers
    if (xSize < 0 || ySize < 0) {
      throw new UnsupportedImageFormatException(
          "Image tile dimensions too large to represent in Java");
    }

    int mode = work.get(); // levelMode + roundingMode x 16

    RoundingMode roundingMode = RoundingMode.values()[mode / 16];
    LevelMode levelMode = LevelMode.values()[mode % 16];
    return new TileDescription(xSize, ySize, levelMode, roundingMode);
  }

  private int getLevelSize(int min, int max, int l) {
    double distance = max - min + 1;
    int scale = 1 << l;
    int size;
    if (roundingMode == RoundingMode.ROUND_UP) {
      size = (int) Math.ceil(distance / scale);
    } else {
      size = (int) Math.floor(distance / scale);
    }
    return Math.max(size, 1);
  }

  public Box2Int getLevelDataWindow(Box2Int dataWindow, int lx, int ly) {
    int maxX =
        dataWindow.getMinX() + getLevelSize(dataWindow.getMinX(), dataWindow.getMaxX(), lx) - 1;
    int maxY =
        dataWindow.getMinY() + getLevelSize(dataWindow.getMinY(), dataWindow.getMaxY(), ly) - 1;
    return new Box2Int(dataWindow.getMinX(), dataWindow.getMinY(), maxX, maxY);
  }

  public Box2Int getTileDataWindow(Box2Int dataWindow, int dx, int dy, int lx, int ly) {
    int tileMinX = dataWindow.getMinX() + dx * xSize;
    int tileMinY = dataWindow.getMinY() + dy * ySize;
    int tileMaxX = tileMinX + xSize - 1;
    int tileMaxY = tileMinY + ySize - 1;
    Box2Int levelWindow = getLevelDataWindow(dataWindow, lx, ly);
    tileMaxX = Math.min(tileMaxX, levelWindow.getMaxX());
    tileMaxY = Math.min(tileMaxY, levelWindow.getMaxY());

    return new Box2Int(tileMinX, tileMinY, tileMaxX, tileMaxY);
  }

  private int roundLog2(int x) {
    if (roundingMode == RoundingMode.ROUND_UP) {
      return Functions.ceilLog2(x);
    } else {
      return Functions.floorLog2(x);
    }
  }

  public int getLevelCountX(Box2Int dataWindow) {
    switch (levelMode) {
    case ONE_LEVEL:
      return 1;
    case MIPMAP_LEVELS:
      return roundLog2(Math.max(dataWindow.width(), dataWindow.height())) + 1;
    case RIPMAP_LEVELS:
      return roundLog2(dataWindow.width()) + 1;
    default:
      throw new IllegalStateException("Shouldn't happen");
    }
  }

  public int getLevelCountY(Box2Int dataWindow) {
    switch (levelMode) {
    case ONE_LEVEL:
      return 1;
    case MIPMAP_LEVELS:
      return roundLog2(Math.max(dataWindow.width(), dataWindow.height())) + 1;
    case RIPMAP_LEVELS:
      return roundLog2(dataWindow.height()) + 1;
    default:
      throw new IllegalStateException("Shouldn't happen");
    }
  }

  private int[] getTileCountsPerLevel(int numLevels, int min, int max, int size) {
    int[] numTiles = new int[numLevels];
    for (int i = 0; i < numLevels; i++) {
      numTiles[i] = (getLevelSize(min, max, i) + size - 1) / size;
    }
    return numTiles;
  }

  public int[][] getTileCounts(Box2Int dataWindow) {
    int numXLevels = getLevelCountX(dataWindow);
    int numYLevels = getLevelCountY(dataWindow);

    int[] xTiles = getTileCountsPerLevel(
        numXLevels, dataWindow.getMinX(), dataWindow.getMaxX(), xSize);
    int[] yTiles = getTileCountsPerLevel(
        numYLevels, dataWindow.getMinY(), dataWindow.getMaxY(), ySize);
    return new int[][] { xTiles, yTiles };
  }

  public int getOffsetTableSize(Box2Int dataWindow) {
    int offsetSize = 0;

    int[][] tileCounts = getTileCounts(dataWindow);
    int[] xTiles = tileCounts[0];
    int[] yTiles = tileCounts[1];

    switch (levelMode) {
    case ONE_LEVEL:
    case MIPMAP_LEVELS:
      for (int i = 0; i < xTiles.length; i++) {
        offsetSize += xTiles[i] * yTiles[i];
      }
      break;
    case RIPMAP_LEVELS:
      for (int i = 0; i < xTiles.length; i++) {
        for (int j = 0; j < yTiles.length; j++) {
          offsetSize += xTiles[i] * yTiles[j];
        }
      }
      break;
    }

    return offsetSize;
  }
}
