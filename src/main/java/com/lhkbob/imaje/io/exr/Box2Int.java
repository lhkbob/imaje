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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Predefined attribute type.
 */
public class Box2Int {
  private final int minX, minY, maxX, maxY;

  public Box2Int(int minX, int minY, int maxX, int maxY) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  public int getMinX() {
    return minX;
  }

  public int getMinY() {
    return minY;
  }

  public int getMaxX() {
    return maxX;
  }

  public int getMaxY() {
    return maxY;
  }

  public int width() {
    return maxX - minX + 1;
  }

  public int height() {
    return maxY - minY + 1;
  }

  public static Box2Int read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // 4 LE ints (16 bytes total) ordered minX, minY, maxX, maxY
    if (!IO.read(in, work, 16)) {
      throw new InvalidImageException("Unexpected EOF while reading Box2Int type");
    }

    int minX = Bytes.bytesToIntLE(work);
    int minY = Bytes.bytesToIntLE(work);
    int maxX = Bytes.bytesToIntLE(work);
    int maxY = Bytes.bytesToIntLE(work);

    Box2Int box = new Box2Int(minX, minY, maxX, maxY);

    if (maxX < minX || maxY < minY) {
      throw new InvalidImageException(
          "Maximum is less than minimum boundary for Box2Int attribute: " + box);
    }
    return box;
  }

  @Override
  public String toString() {
    return String
        .format("Box2Int(w: %d, h: %d, (%d, %d) - (%d, %d))", width(), height(), minX, minY, maxX,
            maxY);
  }
}
