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
 * Predefined attribute describing RGB color space, or set to special values for CIE XYZ stored
 * in r,g,b channels.
 */
public class Chromaticity {
  private final float redX, redY, greenX, greenY, blueX, blueY, whiteX, whiteY;

  public Chromaticity(
      float redX, float redY, float greenX, float greenY, float blueX, float blueY, float whiteX,
      float whiteY) {
    this.redX = redX;
    this.redY = redY;
    this.greenX = greenX;
    this.greenY = greenY;
    this.blueX = blueX;
    this.blueY = blueY;
    this.whiteX = whiteX;
    this.whiteY = whiteY;
  }

  public float getRedX() {
    return redX;
  }

  public float getRedY() {
    return redY;
  }

  public float getGreenX() {
    return greenX;
  }

  public float getGreenY() {
    return greenY;
  }

  public float getBlueX() {
    return blueX;
  }

  public float getBlueY() {
    return blueY;
  }

  public float getWhiteX() {
    return whiteX;
  }

  public float getWhiteY() {
    return whiteY;
  }

  public boolean isCIEXYZ() {
    return Math.abs(redX - 1.0) < 1e-5 && Math.abs(redY) < 1e-5 && Math.abs(greenX) < 1e-5
        && Math.abs(greenY - 1.0) < 1e-5 && Math.abs(blueX) < 1e-5 && Math.abs(blueY) < 1e-5
        && Math.abs(whiteX - 0.33333) < 1e-5 && Math.abs(whiteY - 0.33333) < 1e-5;
  }

  public static Chromaticity read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.read(in, work, 32)) {
      throw new InvalidImageException("Unexpected EOF while reading Chromaticity type");
    }

    float redX = Bytes.bytesToFloatLE(work);
    float redY = Bytes.bytesToFloatLE(work);
    float greenX = Bytes.bytesToFloatLE(work);
    float greenY = Bytes.bytesToFloatLE(work);
    float blueX = Bytes.bytesToFloatLE(work);
    float blueY = Bytes.bytesToFloatLE(work);
    float whiteX = Bytes.bytesToFloatLE(work);
    float whiteY = Bytes.bytesToFloatLE(work);

    return new Chromaticity(redX, redY, greenX, greenY, blueX, blueY, whiteX, whiteY);
  }
}
