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
package com.lhkbob.imaje.io.dds;

import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.lhkbob.imaje.io.dds.DDSHeader.isFlagSet;
import static com.lhkbob.imaje.io.dds.DDSHeader.setFlag;

/**
 * Stores the pixel format information for the dds texture. If the fourCC is valid and set to
 * 'DX10', then the pixel format is stored in a DXGIPixelFormat enum instead of the DX10 header.
 */
public class DDSPixelFormat {
  // Selected bits in DDSPixelFormat flags
  public static final int DDPF_ALPHAPIXELS = 0x00000001; // Alpha channel is present
  public static final int DDPF_ALPHA = 0x00000002; // Only contains alpha information
  public static final int DDPF_LUMINANCE = 0x00020000; // luminance data
  public static final int DDPF_FOURCC = 0x00000004; // FourCC code is valid
  public static final int DDPF_RGB = 0x00000040; // RGB data is present
  public static final int DDPF_YUV = 0x00000200; // YUV data is present in RGB channels

  public static final int FORMAT_LENGTH = 32;

  private int size;
  private int flags;
  private int fourCC;
  private int rgbBitCount;
  private int rBitMask;
  private int gBitMask;
  private int bBitMask;
  private int aBitMask;

  public boolean hasAlphaChannel() {
    return isFlagSet(DDPF_ALPHAPIXELS, getFlags());
  }

  public void setHasAlphaChannel(boolean hasAlpha) {
    setFlags(setFlag(DDPF_ALPHAPIXELS, getFlags(), hasAlpha));
  }

  public boolean isOnlyAlpha() {
    return isFlagSet(DDPF_ALPHA, getFlags());
  }

  public void setOnlyAlpha(boolean onlyAlpha) {
    setFlags(setFlag(DDPF_ALPHA, getFlags(), onlyAlpha));
  }

  public boolean isLuminance() {
    return isFlagSet(DDPF_LUMINANCE, getFlags());
  }

  public void setLuminance(boolean isLuminance) {
    setFlags(setFlag(DDPF_LUMINANCE, getFlags(), isLuminance));
  }

  public boolean isRGB() {
    return isFlagSet(DDPF_RGB, getFlags());
  }

  public void setRGB(boolean isRGB) {
    setFlags(setFlag(DDPF_RGB, getFlags(), isRGB));
  }

  public boolean isYUV() {
    return isFlagSet(DDPF_YUV, getFlags());
  }

  public void setYUV(boolean isYUV) {
    setFlags(setFlag(DDPF_YUV, getFlags(), isYUV));
  }

  public boolean isFourCCValid() {
    return isFlagSet(DDPF_FOURCC, getFlags());
  }

  public void setFourCCValid(boolean valid) {
    setFlags(setFlag(DDPF_FOURCC, getFlags(), valid));
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getFlags() {
    return flags;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

  public int getFourCC() {
    return fourCC;
  }

  public void setFourCC(int fourCC) {
    this.fourCC = fourCC;
  }

  public int getRGBBitCount() {
    return rgbBitCount;
  }

  public void setRGBBitCount(int rgbBitCount) {
    this.rgbBitCount = rgbBitCount;
  }

  public int getRedBitMask() {
    return rBitMask;
  }

  public void setRedBitMask(int mask) {
    rBitMask = mask;
  }

  public int getGreenBitMask() {
    return gBitMask;
  }

  public void setGreenBitMask(int mask) {
    gBitMask = mask;
  }

  public int getBlueBitMask() {
    return bBitMask;
  }

  public void setBlueBitMask(int mask) {
    bBitMask = mask;
  }

  public int getAlphaBitMask() {
    return aBitMask;
  }

  public void setAlphaBitMask(int mask) {
    aBitMask = mask;
  }

  public static DDSPixelFormat parse(ByteBuffer work) throws IOException {
    if (work.remaining() < FORMAT_LENGTH) {
      throw new InvalidImageException("Insufficient bytes remaining to parse DDSPixelFormat");
    }

    DDSPixelFormat pixelFormat = new DDSPixelFormat();
    pixelFormat.size = Bytes.bytesToIntLE(work);
    pixelFormat.flags = Bytes.bytesToIntLE(work);
    pixelFormat.fourCC = Bytes.bytesToIntLE(work);
    pixelFormat.rgbBitCount = Bytes.bytesToIntLE(work);
    pixelFormat.rBitMask = Bytes.bytesToIntLE(work);
    pixelFormat.gBitMask = Bytes.bytesToIntLE(work);
    pixelFormat.bBitMask = Bytes.bytesToIntLE(work);
    pixelFormat.aBitMask = Bytes.bytesToIntLE(work);
    return pixelFormat;
  }
}
