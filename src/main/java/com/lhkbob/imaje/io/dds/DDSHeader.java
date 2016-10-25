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
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * // Stores a DDS header (equivalent for DX9 and DX10, DX10 may have non-null DX10Header, too)
 */
public class DDSHeader {
  public static final int CAPS_FIELD_LENGTH = 4;
  // Selected bits in DDS capabilities 2 flags
  public static final int DDSCAPS2_CUBEMAP = 0x00000200;
  public static final int DDSCAPS2_CUBEMAP_ALL_FACES = 0x0000fc00;
  public static final int DDSCAPS2_CUBEMAP_NEGATIVEX = 0x00000800;
  public static final int DDSCAPS2_CUBEMAP_NEGATIVEY = 0x00002000;
  public static final int DDSCAPS2_CUBEMAP_NEGATIVEZ = 0x00008000;
  public static final int DDSCAPS2_CUBEMAP_POSITIVEX = 0x00000400;
  public static final int DDSCAPS2_CUBEMAP_POSITIVEY = 0x00001000;
  public static final int DDSCAPS2_CUBEMAP_POSITIVEZ = 0x00004000;
  public static final int DDSCAPS2_VOLUME = 0x00200000;
  public static final int DDSCAPS_COMPLEX = 0x00000008; // Complex image structure such as a cubemap
  public static final int DDSCAPS_MIPMAP = 0x00400000; // Has mipmaps
  // Selected bits in DDS capabilities flags
  public static final int DDSCAPS_TEXTURE = 0x00001000; // Can be used as a texture
  // Selected bits in DDSHeader flags
  public static final int DDSD_CAPS = 0x00000001; // Capabilities are valid
  public static final int DDSD_DEPTH = 0x00800000; // dwDepth is valid
  public static final int DDSD_HEIGHT = 0x00000002; // Height is valid
  public static final int DDSD_LINEARSIZE = 0x00080000; // dwLinearSize is valid
  public static final int DDSD_MIPMAPCOUNT = 0x00020000; // Mipmap count is valid
  public static final int DDSD_PITCH = 0x00000008; // Pitch is valid
  public static final int DDSD_PIXELFORMAT = 0x00001000; // ddpfPixelFormat is valid
  public static final int DDSD_WIDTH = 0x00000004; // Width is valid
  public static final int HEADER_LENGTH = 124;
  public static final int RESERVED_FIELD1_LENGTH = 11;

  // Named caps, caps2, caps3, and caps4 (technically not an array)
  private final int[] caps = new int[CAPS_FIELD_LENGTH];
  private final int[] reserved1 = new int[RESERVED_FIELD1_LENGTH];
  private int depth;
  private int flags;
  // Not really part of the header, but it follows immediately. Not null if this is a DX10 dds
  // texture, i.e. pixelFormat.fourCC == 'DX10'.
  private DX10Header headerDX10;
  private int height;
  private int linearSize;
  private int magic;
  private int mipmapCount;
  // DDSHeaders always have a pixelFormat, so this cannot be null
  private DDSPixelFormat pixelFormat;
  // First set of reserved bits are the 11 bits in reserved1, hence this name: reserved2
  private int reserved2;
  private int size;
  private int width;

  public DDSPixelFormat getPixelFormat() {
    return pixelFormat;
  }

  public void setPixelFormat(DDSPixelFormat format) {
    Arguments.notNull("format", format);
    pixelFormat = format;
  }

  public int getCapabilitiesBitField(int field) {
    return caps[field];
  }

  public DX10Header getDX10Header() {
    return headerDX10;
  }

  public int getDepth() {
    return depth;
  }

  public int getFlags() {
    return flags;
  }

  public int getHeaderSize() {
    return size;
  }

  public int getHeight() {
    return height;
  }

  public int getLinearSize() {
    return linearSize;
  }

  public int getMagicNumber() {
    return magic;
  }

  public int getMipmapCount() {
    return mipmapCount;
  }

  public int getReservedBitField(int field) {
    if (field == reserved1.length) {
      return reserved2;
    } else {
      return reserved1[field];
    }
  }

  public int getWidth() {
    return width;
  }

  public boolean hasAllCubeFaces() {
    return isFlagSet(DDSCAPS2_CUBEMAP_ALL_FACES, getCapabilitiesBitField(1));
  }

  public boolean hasCubeFaceNegativeX() {
    return isFlagSet(DDSCAPS2_CUBEMAP_NEGATIVEX, getCapabilitiesBitField(1));
  }

  public boolean hasCubeFaceNegativeY() {
    return isFlagSet(DDSCAPS2_CUBEMAP_NEGATIVEY, getCapabilitiesBitField(1));
  }

  public boolean hasCubeFaceNegativeZ() {
    return isFlagSet(DDSCAPS2_CUBEMAP_NEGATIVEZ, getCapabilitiesBitField(1));
  }

  public boolean hasCubeFacePositiveX() {
    return isFlagSet(DDSCAPS2_CUBEMAP_POSITIVEX, getCapabilitiesBitField(1));
  }

  public boolean hasCubeFacePositiveY() {
    return isFlagSet(DDSCAPS2_CUBEMAP_POSITIVEY, getCapabilitiesBitField(1));
  }

  public boolean hasCubeFacePositiveZ() {
    return isFlagSet(DDSCAPS2_CUBEMAP_POSITIVEZ, getCapabilitiesBitField(1));
  }

  public boolean isCapabilitiesValid() {
    return isFlagSet(getFlags(), DDSD_CAPS);
  }

  public boolean isComplex() {
    return isFlagSet(DDSCAPS_COMPLEX, getCapabilitiesBitField(0));
  }

  public boolean isCubeMap() {
    return isFlagSet(DDSCAPS2_CUBEMAP, getCapabilitiesBitField(1));
  }

  public boolean isDepthValid() {
    return isFlagSet(getFlags(), DDSD_DEPTH);
  }

  public boolean isHeightValid() {
    return isFlagSet(getFlags(), DDSD_HEIGHT);
  }

  public boolean isLinearSizeValid() {
    return isFlagSet(getFlags(), DDSD_LINEARSIZE);
  }

  public boolean isMipmapCountValid() {
    return isFlagSet(getFlags(), DDSD_MIPMAPCOUNT);
  }

  public boolean isMipmapped() {
    return isFlagSet(DDSCAPS_MIPMAP, getCapabilitiesBitField(0));
  }

  public boolean isPitchValid() {
    return isFlagSet(getFlags(), DDSD_PITCH);
  }

  public boolean isPixelFormatValid() {
    return isFlagSet(getFlags(), DDSD_PIXELFORMAT);
  }

  public boolean isTexture() {
    return isFlagSet(DDSCAPS_TEXTURE, getCapabilitiesBitField(0));
  }

  public boolean isVolume() {
    return isFlagSet(DDSCAPS2_VOLUME, getCapabilitiesBitField(1));
  }

  public boolean isWidthValid() {
    return isFlagSet(getFlags(), DDSD_WIDTH);
  }

  public void setCapabilitiesBitField(int field, int bits) {
    caps[field] = bits;
  }

  public void setCapabilitiesValid(boolean valid) {
    setFlags(setFlag(DDSD_CAPS, getFlags(), valid));
  }

  public void setComplex(boolean isComplex) {
    setCapabilitiesBitField(0, setFlag(DDSCAPS_COMPLEX, getCapabilitiesBitField(0), isComplex));
  }

  public void setCubeMap(boolean isCubeMap) {
    setCapabilitiesBitField(1, setFlag(DDSCAPS2_CUBEMAP, getCapabilitiesBitField(1), isCubeMap));
  }

  public void setDX10Header(@Arguments.Nullable DX10Header h) {
    headerDX10 = h;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public void setDepthValid(boolean valid) {
    setFlags(setFlag(DDSD_DEPTH, getFlags(), valid));
  }

  public void setFlags(int bits) {
    flags = bits;
  }

  public void setHasAllCubeFaces(boolean hasFaces) {
    setCapabilitiesBitField(
        1, setFlag(DDSCAPS2_CUBEMAP_ALL_FACES, getCapabilitiesBitField(1), hasFaces));
  }

  public void setHasCubeFaceNegativeX(boolean hasFace) {
    setCapabilitiesBitField(
        1, setFlag(DDSCAPS2_CUBEMAP_NEGATIVEX, getCapabilitiesBitField(1), hasFace));
  }

  public void setHasCubeFaceNegativeY(boolean hasFace) {
    setCapabilitiesBitField(
        1, setFlag(DDSCAPS2_CUBEMAP_NEGATIVEY, getCapabilitiesBitField(1), hasFace));
  }

  public void setHasCubeFaceNegativeZ(boolean hasFace) {
    setCapabilitiesBitField(
        1, setFlag(DDSCAPS2_CUBEMAP_NEGATIVEZ, getCapabilitiesBitField(1), hasFace));
  }

  public void setHasCubeFacePositiveX(boolean hasFace) {
    setCapabilitiesBitField(
        1, setFlag(DDSCAPS2_CUBEMAP_POSITIVEX, getCapabilitiesBitField(1), hasFace));
  }

  public void setHasCubeFacePositiveY(boolean hasFace) {
    setCapabilitiesBitField(
        1, setFlag(DDSCAPS2_CUBEMAP_POSITIVEY, getCapabilitiesBitField(1), hasFace));
  }

  public void setHasCubeFacePositiveZ(boolean hasFace) {
    setCapabilitiesBitField(
        1, setFlag(DDSCAPS2_CUBEMAP_POSITIVEZ, getCapabilitiesBitField(1), hasFace));
  }

  public void setHeaderSize(int size) {
    this.size = size;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setHeightValid(boolean valid) {
    setFlags(setFlag(DDSD_HEIGHT, getFlags(), valid));
  }

  public void setLinearSize(int size) {
    linearSize = size;
  }

  public void setLinearSizeValid(boolean valid) {
    setFlags(setFlag(DDSD_LINEARSIZE, getFlags(), valid));
  }

  public void setMagicNumber(int magic) {
    this.magic = magic;
  }

  public void setMipmapCount(int mipmapCount) {
    this.mipmapCount = mipmapCount;
  }

  public void setMipmapCountValid(boolean valid) {
    setFlags(setFlag(DDSD_MIPMAPCOUNT, getFlags(), valid));
  }

  public void setMipmapped(boolean isMipmapped) {
    setCapabilitiesBitField(0, setFlag(DDSCAPS_MIPMAP, getCapabilitiesBitField(0), isMipmapped));
  }

  public void setPitchValid(boolean valid) {
    setFlags(setFlag(DDSD_PITCH, getFlags(), valid));
  }

  public void setPixelFormatValid(boolean valid) {
    setFlags(setFlag(DDSD_PIXELFORMAT, getFlags(), valid));
  }

  public void setReservedBitField(int field, int bits) {
    if (field == reserved1.length) {
      reserved2 = bits;
    } else {
      reserved1[field] = bits;
    }
  }

  public void setTexture(boolean isTexture) {
    setCapabilitiesBitField(0, setFlag(DDSCAPS_TEXTURE, getCapabilitiesBitField(0), isTexture));
  }

  public void setVolume(boolean isVolume) {
    setCapabilitiesBitField(1, setFlag(DDSCAPS2_VOLUME, getCapabilitiesBitField(1), isVolume));
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setWidthValid(boolean valid) {
    setFlags(setFlag(DDSD_WIDTH, getFlags(), valid));
  }

  public static DDSHeader readHeader(SeekableByteChannel in) throws IOException {
    // Magic number is 4 bytes, header is 124 bytes, and DX10 header is 20 bytes = 148 maximum
    // just to read the header. However, the rest of the data will be mapped into memory and
    // copied directly so there is no need to use a conventional work buffer that is quite large
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(HEADER_LENGTH + DX10Header.HEADER_LENGTH + 4);

    DDSHeader h = new DDSHeader();

    // Read first 128 bytes, which all valid DDS files must have
    if (!IO.read(in, work, 4)) {
      throw new InvalidImageException("EOF reached before magic number could be parsed");
    }
    h.magic = Bytes.bytesToIntLE(work);

    if (!IO.read(in, work, HEADER_LENGTH)) {
      throw new InvalidImageException("EOF reached before end of DDS header");
    }

    h.size = Bytes.bytesToIntLE(work);
    h.flags = Bytes.bytesToIntLE(work);
    h.height = Bytes.bytesToIntLE(work);
    h.width = Bytes.bytesToIntLE(work);
    h.linearSize = Bytes.bytesToIntLE(work);
    h.depth = Bytes.bytesToIntLE(work);
    h.mipmapCount = Bytes.bytesToIntLE(work);
    for (int i = 0; i < h.reserved1.length; i++) {
      h.reserved1[i] = Bytes.bytesToIntLE(work);
    }

    h.pixelFormat = DDSPixelFormat.parse(work);

    h.caps[0] = Bytes.bytesToIntLE(work);
    h.caps[1] = Bytes.bytesToIntLE(work);
    h.caps[2] = Bytes.bytesToIntLE(work);
    h.caps[3] = Bytes.bytesToIntLE(work);

    h.reserved2 = Bytes.bytesToIntLE(work);
    // This ends the 128 bytes that were previously read

    if (h.pixelFormat.getFourCC() == FOURCC_DX10) {
      h.headerDX10 = DX10Header.read(in, work);
    } else {
      h.headerDX10 = null;
    }

    h.validate();
    return h;
  }

  // check whether or not flag is set in the flags bit field
  static boolean isFlagSet(int flag, int flags) {
    return (flags & flag) == flag;
  }

  // Create a 4cc code from the given string. The string must have length = 4
  static int makeFourCC(String c) {
    if (c.length() != 4) {
      throw new IllegalArgumentException("Input string for a 4CC must have size of 4");
    }
    char[] cc = c.toCharArray();
    return ((cc[3] & 0xff) << 24) | ((cc[2] & 0xff) << 16) | ((cc[1] & 0xff) << 8) | ((cc[0]
        & 0xff));
  }

  static int setFlag(int flag, int currentFlags, boolean value) {
    int maskedFlags = currentFlags & ~flag;
    int flagValue = value ? flag : 0;
    return maskedFlags | flagValue;
  }

  // Convert a 4cc code back into string form
  static String unmakeFourCC(int fourcc) {
    char[] cc = new char[4];
    cc[3] = (char) ((fourcc & 0xff000000) >> 24);
    cc[2] = (char) ((fourcc & 0xff0000) >> 16);
    cc[1] = (char) ((fourcc & 0xff00) >> 8);
    cc[0] = (char) ((fourcc & 0xff));
    return new String(cc);
  }

  private void validate() throws InvalidImageException {
    // Must have the magic number 'DDS '. Size must be 124, although devIL reports that some files
    // have 'DDS ' in the size var as well, so we'll support that.
    if (magic != FOURCC_DDS || (size != HEADER_LENGTH && size != FOURCC_DDS)) {
      throw new InvalidImageException("DDS header is invalid");
    }
    if (pixelFormat.getSize() != DDSPixelFormat.FORMAT_LENGTH) {
      throw new InvalidImageException("DDS pixel format header is invalid");
    }

    // DDSD_CAPS, DDSD_PIXELFORMAT should be set in flags, but that is not validated
    // DDSCAPS_TEXTURE should be set in caps but that is not validated either.

    // DDSCAPS_COMPLEX should be set in caps if MIPMAP, VOLUME, or CUBEMAP are set, but that is
    // not consistently set by file writers.

    // Header flags will be further validated as the header is interpreted
  }
  // Magic number for the file
  private static final int FOURCC_DDS = makeFourCC("DDS ");
  // Special FOURCC code that designates a DX10 header is after the regular header
  private static final int FOURCC_DX10 = makeFourCC("DX10");
}
