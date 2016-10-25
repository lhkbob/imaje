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

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Mipmap;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.BitData;
import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.data.types.CustomBinaryData;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;
import com.lhkbob.imaje.layout.DataLayout;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.UnpackedPixelArray;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 *
 */
public abstract class AbstractChunkReader implements ChunkReader {
  private final OpenEXRHeader header;
  private final ChannelMapping mapping;
  private final Data.Factory dataFactory;

  // If not actually mipmapped then the list will be a single element
  private List<NumericData<?>> mipmapData;

  private byte[] work1;
  private byte[] work2;

  protected AbstractChunkReader(Data.Factory dataFactory, ChannelMapping mapping) {
    header = mapping.getHeader();
    this.mapping = mapping;
    this.dataFactory = dataFactory;
  }

  public static EnumSet<Compression> getSupportedCompressions() {
    return EnumSet.of(Compression.NONE, Compression.ZIP, Compression.ZIPS);
  }

  @Override
  public OpenEXRHeader getHeader() {
    return header;
  }

  @Override
  public ChannelMapping getMapping() {
    return mapping;
  }

  @Override
  public void initialize() {
    int workLen = getMaxUncompressedDataSize();
    work1 = new byte[workLen];
    work2 = new byte[workLen];

    mipmapData = createBackingData();
  }

  @Override
  public List<? extends Image<?>> getImages() {
    Map<PixelFormat, Class<? extends Color>> formats = mapping.getAllFormats();
    if (mipmapData.size() > 1) {
      // Images will be mipmapped
      List<Mipmap<?>> images = new ArrayList<>(formats.size());

      int mipmapCount = mipmapData.size();
      for (Map.Entry<PixelFormat, Class<? extends Color>> i : formats.entrySet()) {
        List<PixelArray> mips = new ArrayList<>(mipmapCount);
        for (int j = 0; j < mipmapCount; j++) {
          mips.add(
              new UnpackedPixelArray(i.getKey(), header.getLayoutForMipmap(j), mipmapData.get(j)));
        }

        images.add(createMipmap(i.getValue(), mips));
      }

      return images;
    } else {
      // Images will be rasters, so they have a shared data buffer and layout
      DataLayout layout = header.getLayoutForLevel(0, 0);
      NumericData<?> sharedData = mipmapData.get(0);

      List<Raster<?>> images = new ArrayList<>(formats.size());
      for (Map.Entry<PixelFormat, Class<? extends Color>> i : formats.entrySet()) {
        images.add(createRaster(i.getValue(), i.getKey(), layout, sharedData));
      }

      return images;
    }
  }

  protected NumericData<?> getDataForLevel(int level) {
    return mipmapData.get(level);
  }

  private static <T extends Color> Mipmap<T> createMipmap(Class<T> color, List<PixelArray> mips) {
    return new Mipmap<>(color, mips);
  }

  private static <T extends Color> Raster<T> createRaster(
      Class<T> color, PixelFormat format, DataLayout layout, NumericData<?> data) {
    return new Raster<>(color, new UnpackedPixelArray(format, layout, data));
  }

  protected void readChunk(
      int dataSize, Box2Int chunkWindow, NumericData<?> image, DataLayout layout,
      SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.fill(work1, dataSize, in, work)) {
      throw new InvalidImageException("Unable to fully read tile chunk");
    }

    // The data will be  copied into the image data array at the offset for the first channel of the
    // layout for the top left corner of the data window.
    long offset = layout.getChannelIndex(chunkWindow.getMinX(), chunkWindow.getMinY(), 0) * header
        .getBytesPerPixel();

    int uncompressedSize = header.getBytesPerPixel() * chunkWindow.width() * chunkWindow.height();

    Compression lineCompression = header.getCompression();
    if (dataSize >= uncompressedSize) {
      // The compression didn't succeed for this line block, so uncompressed data is stored
      lineCompression = Compression.NONE;
    }

    switch (lineCompression) {
    case NONE:
      // The work1 data array holds the data uncompressed so it can be used directly
      copyUncompressedData(work1, uncompressedSize, image, offset);
      break;
    case ZIP:
    case ZIPS:
      // These both use the ZIP compression algorithm, they just differ in line block height,
      // which has already been encoded into the data window's height and thus uncompressedSize
      byte[] uncompressed = unzipBlock(work1, work2, uncompressedSize);
      copyUncompressedData(uncompressed, uncompressedSize, image, offset);
      break;
    case PIZ:
    case RLE:
      // FIXME I have intentions to implement RLE and PIZ
    case PXR24:
    case B44:
    case B44A:
      throw new UnsupportedImageFormatException(
          "Compression mode is not supported: " + header.getCompression());
    }
  }

  private void copyUncompressedData(
      byte[] data, int dataLength, NumericData<?> image, long offset) {
    BitData bits = image.asBitData();
    // Copy byte data manually into data source, accounting for little endian order of OpenEXR files
    if (bits instanceof ByteData) {
      ByteData d = (ByteData) bits;
      d.set(offset, data, 0, dataLength);
    } else if (bits instanceof ShortData) {
      ShortData d = (ShortData) bits;
      for (int i = 0; i < dataLength; i += 2) {
        short v = Bytes.bytesToShortLE(data, i);
        d.set(offset + i / 2, v);
      }
    } else if (bits instanceof IntData) {
      IntData d = (IntData) bits;
      for (int i = 0; i < dataLength; i += 4) {
        int v = Bytes.bytesToIntLE(data, i);
        d.set(offset + i / 4, v);
      }
    } else if (bits instanceof LongData) {
      LongData d = (LongData) bits;
      for (int i = 0; i < dataLength; i += 8) {
        long v = Bytes.bytesToLongLE(data, i);
        d.set(offset + i / 8, v);
      }
    } else {
      // Should not happen
      throw new UnsupportedOperationException("Unexpected bit data type: " + bits);
    }
  }

  private byte[] unzipBlock(byte[] compressed, byte[] temp, int uncompressedLength) {
    Inflater decompressor = new Inflater();
    decompressor.setInput(compressed);
    int read = 0;
    while (read < uncompressedLength) {
      try {
        read += decompressor.inflate(temp, read, uncompressedLength - read);
      } catch (DataFormatException e) {
        throw new IllegalStateException("Expected ZIP formatted scanline block", e);
      }
    }
    decompressor.end();

    // predictor (FIXME I don't know what this means, but it's from ImfZip.cpp
    for (int i = 1; i < uncompressedLength; i++) {
      int d = (0xff & temp[i - 1]) + (0xff & temp[i]) - 128;
      temp[i] = (byte) d;
    }

    // reorder the pixel data
    int i1 = 0;
    int i2 = (uncompressedLength + 1) / 2;
    int i = 0;
    while (true) {
      if (i < uncompressedLength) {
        compressed[i++] = temp[i1++];
      } else {
        break;
      }
      if (i < uncompressedLength) {
        compressed[i++] = temp[i2++];
      } else {
        break;
      }
    }

    return compressed;
  }

  protected NumericData<?> createData(long numPixels) {
    // Do not use getBytesPerPixel() since this length is being passed directly to creation
    // functions that create multi-byte types.
    long bufferLength = numPixels * header.getChannels().size();

    switch (header.getChannels().get(0).getFormat()) {
    case UINT:
      return new CustomBinaryData<>(Data.UINT32, dataFactory.newIntData(bufferLength));
    case HALF:
      return new CustomBinaryData<>(Data.SFLOAT16, dataFactory.newShortData(bufferLength));
    case FLOAT:
      return dataFactory.newFloatData(bufferLength);
    default:
      // Should not happen unless this code hasn't been updated after a new ChannelFormat is added
      throw new UnsupportedOperationException(
          "Unrecognized channel pixel format: " + header.getChannels().get(0).getFormat());
    }
  }

  protected abstract int getMaxUncompressedDataSize();

  protected abstract List<NumericData<?>> createBackingData();
}
