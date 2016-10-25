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
import com.lhkbob.imaje.io.IO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static com.lhkbob.imaje.io.dds.DDSHeader.isFlagSet;
import static com.lhkbob.imaje.io.dds.DDSHeader.setFlag;

/**
 * Only present if header.pixelFormat.fourCC=='DX10'
 */
public class DX10Header {
  public static final int HEADER_LENGTH = 20;

  public enum D3D10ResourceDimension {
    UNKNOWN, BUFFER, TEXTURE1D, TEXTURE2D, TEXTURE3D
  }

  // Selected bits in DDSHeaderDX10 misc flags
  public static final int D3D10_MISC_RESOURCE_GENERATE_MIPS = 0x1;
  public static final int D3D10_MISC_RESOURCE_SHARED = 0x2;
  public static final int D3D10_MISC_RESOURCE_TEXTURECUBE = 0x4;

  private DXGIFormat dxgiFormat;
  private D3D10ResourceDimension resourceDimension;

  private int miscFlag;
  private int arraySize;
  private int reserved;

  public boolean areMipmapsGenerated() {
    return isFlagSet(D3D10_MISC_RESOURCE_GENERATE_MIPS, getMiscellaneousFlags());
  }

  public void setMipmapsGenerated(boolean generate) {
    setMiscellaneousFlags(
        setFlag(D3D10_MISC_RESOURCE_GENERATE_MIPS, getMiscellaneousFlags(), generate));
  }

  public boolean isShared() {
    return isFlagSet(D3D10_MISC_RESOURCE_SHARED, getMiscellaneousFlags());
  }

  public void setShared(boolean shared) {
    setMiscellaneousFlags(setFlag(D3D10_MISC_RESOURCE_SHARED, getMiscellaneousFlags(), shared));
  }

  public boolean isCubeMap() {
    return isFlagSet(D3D10_MISC_RESOURCE_TEXTURECUBE, getMiscellaneousFlags());
  }

  public void setCubeMap(boolean cubeMap) {
    setMiscellaneousFlags(
        setFlag(D3D10_MISC_RESOURCE_TEXTURECUBE, getMiscellaneousFlags(), cubeMap));
  }

  public DXGIFormat getDXGIFormat() {
    return dxgiFormat;
  }

  public void setDXGIFormat(DXGIFormat format) {
    dxgiFormat = format;
  }

  public D3D10ResourceDimension getResourceDimension() {
    return resourceDimension;
  }

  public void setResourceDimension(D3D10ResourceDimension resourceDimension) {
    this.resourceDimension = resourceDimension;
  }

  public int getMiscellaneousFlags() {
    return miscFlag;
  }

  public void setMiscellaneousFlags(int flag) {
    miscFlag = flag;
  }

  public int getArraySize() {
    return arraySize;
  }

  public void setArraySize(int size) {
    arraySize = size;
  }

  public int getReservedBits() {
    return reserved;
  }

  public void setReservedBits(int reserved) {
    this.reserved = reserved;
  }

  public static DX10Header read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // Read additional bytes
    IO.read(in, work, HEADER_LENGTH);

    DX10Header headerDX10 = new DX10Header();
    int dxgi = Bytes.bytesToIntLE(work);
    if (dxgi < 0 || dxgi >= DXGIFormat.values().length) {
      headerDX10.dxgiFormat = DXGIFormat.values()[dxgi];
    } else {
      headerDX10.dxgiFormat = DXGIFormat.UNKNOWN;
    }

    int resourceDimension = Bytes.bytesToIntLE(work);
    headerDX10.resourceDimension = D3D10ResourceDimension.values()[resourceDimension];
    headerDX10.miscFlag = Bytes.bytesToIntLE(work);
    headerDX10.arraySize = Bytes.bytesToIntLE(work);
    headerDX10.reserved = Bytes.bytesToIntLE(work);

    return headerDX10;
  }
}
