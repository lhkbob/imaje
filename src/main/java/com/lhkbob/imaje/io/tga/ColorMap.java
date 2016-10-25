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
package com.lhkbob.imaje.io.tga;

import com.lhkbob.imaje.io.IO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static com.lhkbob.imaje.io.tga.TGAFormat.getUnsignedByte;
import static com.lhkbob.imaje.io.tga.TGAFormat.getUnsignedShort;

/**
 *
 */
public class ColorMap {
  private final boolean ubyteIndexed;
  private final byte[] colorMapData;
  private final int elementByteCount;
  private final int numElements;
  private final int startIndex;

  public ColorMap(TGAHeader h) {
    startIndex = h.getColorMapFirstEntry();
    // Color map entry size is listed in bits, so divide by 8
    elementByteCount = h.getColorMapEntrySize() >> 3;
    numElements = h.getColorMapLength();
    colorMapData = new byte[numElements * elementByteCount];
    ubyteIndexed = h.getPixelDepth() == 8;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public int getElementByteSize() {
    return elementByteCount;
  }

  public int getElementCount() {
    return numElements;
  }

  public byte[] getColorMapData() {
    return colorMapData;
  }

  public int getNextDataIndex(ByteBuffer data) {
    int rawIndex = (ubyteIndexed ? getUnsignedByte(data) : getUnsignedShort(data));
    return elementByteCount * (rawIndex - startIndex);
  }

  public static ColorMap read(TGAHeader h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ColorMap cm = new ColorMap(h);
    IO.fill(cm.colorMapData, in, work);
    return cm;
  }
}
