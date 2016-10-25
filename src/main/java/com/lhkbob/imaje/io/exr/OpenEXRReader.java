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
import com.lhkbob.imaje.MipmapArray;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.RasterArray;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Generic;
import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.ImageFileReader;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.SubImagePixelArray;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.lhkbob.imaje.io.exr.TypeReader.readAll;


/**
 * i. FIXME check OpenEXRs' definition of luminance/chroma to see if it's compatible with YUV or something
 * 11. FIXME how to handle chromaticity reported for RGB.Linear? Same problem in Radiance image files.
 */
public class OpenEXRReader implements ImageFileReader {
  private static final int WORK_BUFFER_LEN = 2048;

  private final Data.Factory dataFactory;

  public OpenEXRReader() {
    this(null);
  }

  public OpenEXRReader(Data.Factory dataFactory) {
    if (dataFactory == null) {
      dataFactory = Data.getDefaultDataFactory();
    }
    this.dataFactory = dataFactory;
  }

  @Override
  public Image<?> read(SeekableByteChannel in) throws IOException {
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(WORK_BUFFER_LEN);

    ImageFormat format = readPreHeader(in, work);
    List<OpenEXRHeader> headers = readHeaders(format, in, work);
    Map<OpenEXRHeader, ChunkReader> readers = createChunkReaders(headers);

    Map<OpenEXRHeader, OffsetTable> offsets = readOffsetTables(headers, readers, in, work);
    Map<OpenEXRHeader, List<? extends Image<?>>> chunks = readAllChunks(
        format, headers, readers, offsets, in, work);

    // At least for the time being, this reader won't remember header specific images
    List<Image<?>> allChunks = new ArrayList<>();
    chunks.values().forEach(allChunks::addAll);

    OpenEXRHeader mainHeader = chunks.keySet().iterator().next();
    return consolidateChunks(allChunks, mainHeader.getDisplayWindow());
  }

  private void filterChunks(List<Image<?>> allChunks, Function<Image<?>, Boolean> filter) {
    int trueImageCount = 0;
    for (Image<?> img : allChunks) {
      if (filter.apply(img)) {
        trueImageCount++;
      }
    }

    if (trueImageCount > 0 && trueImageCount != allChunks.size()) {
      // There are more true-filtered image layers than not so keep just the true ones
      boolean keepTrue = trueImageCount > allChunks.size() - trueImageCount;
      Iterator<Image<?>> it = allChunks.iterator();
      while (it.hasNext()) {
        Image<?> img = it.next();
        if (filter.apply(img) != keepTrue) {
          // State does not match the filter
          it.remove();
        }
      }
    }
  }

  private Image<?> consolidateChunks(List<Image<?>> allChunks, Box2Int displayWindow) {
    // There is a whole potential slew of images that can be combined together.
    // 1. If the images are a mix of mipmapped vs. raster, take the majority.
    // 2. If the images have a mix of alpha channels, take the majority.
    // 2. If all the images are of the same color type, they can be converted to an array image
    //    of that type. If they at least have the same channel count, the color type is cast to a
    //    generic color type. Otherwise take the majority.
    // 3. All images are windowed based on the display window, which makes all header images the
    //    exact size. At this point, every image has the same color type and mipmap-ness, and is
    //    either array'ed or not, so all layers can be collected into a single list to form one
    //    large array.

    // Check mipmap state
    filterChunks(allChunks, Image::isMipmapped);

    // Check alpha state
    filterChunks(allChunks, Image::hasAlphaChannel);

    // Now check color channel type (which is a strict super set of color matching type, so it can
    // be done first without affecting color choice).
    Map<Integer, Integer> channelCountHistogram = new HashMap<>();
    for (Image<?> img : allChunks) {
      int channelCount = Color.getChannelCount(img.getColorType());
      Integer count = channelCountHistogram.get(channelCount);
      if (count == null) {
        count = 1;
      } else {
        count = count + 1;
      }
      channelCountHistogram.put(channelCount, count);
    }

    // Find the channel count that preserves the most images
    int maxChannels = -1;
    int maxChannelImages = -1;
    for (Map.Entry<Integer, Integer> e : channelCountHistogram.entrySet()) {
      if (e.getValue() > maxChannelImages) {
        // This channel count has more images preserved
        maxChannelImages = e.getValue();
        maxChannels = e.getKey();
      } else if (e.getValue() == maxChannelImages && e.getKey() > maxChannels) {
        // This channel count has the same image count preserved, but actually more channels so
        // in the interest in preserving more "data" take the higher channel count.
        maxChannelImages = e.getValue();
        maxChannels = e.getKey();
      }
    }

    final int maxSelectedChannels = maxChannels;
    filterChunks(
        allChunks, img -> Color.getChannelCount(img.getColorType()) == maxSelectedChannels);

    // At this point all images have the same mipmap state, same alpha channel state, and same
    // number of color channels. If they share the same color class then that can be the color
    // class, otherwise convert to a generic class
    Class<? extends Color> colorType = null;
    for (Image<?> img : allChunks) {
      if (colorType == null) {
        colorType = img.getColorType();
      } else if (!colorType.equals(img.getColorType())) {
        // Set back to null to flag the color set as non-singleton
        colorType = null;
        break;
      }
    }

    if (colorType == null) {
      // Must convert to generic, so pick a class based on the number of channels
      switch (maxSelectedChannels) {
      case 1:
        colorType = Generic.C1.class;
        break;
      case 2:
        colorType = Generic.C2.class;
        break;
      case 3:
        colorType = Generic.C3.class;
        break;
      case 4:
        colorType = Generic.C4.class;
        break;
      // FIXME could we assume Spectrum in this case for more channel count options...?

      default:
        // Unfortunately arbitrary channel count is not supported. Instead of failing at this point,
        // discard all images except the first as a simple fallback
        allChunks = Collections.singletonList(allChunks.get(0));
        colorType = allChunks.get(0).getColorType();
        break;
      }
    }

    return makeFinalImage(colorType, allChunks, displayWindow);
  }

  private <T extends Color> Image<T> makeFinalImage(
      Class<T> color, List<Image<?>> compatibleImages, Box2Int displayWindow) {
    if (compatibleImages.get(0).isMipmapped()) {
      // Mipmap or MipmapArray depending on size of list
      List<Mipmap<T>> mipmaps = new ArrayList<>(compatibleImages.size());
      for (Image<?> i : compatibleImages) {
        List<PixelArray> m = ((Mipmap<?>) i).getPixelArrays();
        if (i.getWidth() != displayWindow.width() || i.getHeight() != displayWindow.height()) {
          // Make a subimage
          m = SubImagePixelArray
              .createSubImagesForMipmap(m, displayWindow.getMinX(), displayWindow.getMinY(),
                  displayWindow.width(), displayWindow.height());
        }
        mipmaps.add(new Mipmap<>(color, m));
      }

      // If there's only one image return the mipmap as is, otherwise turn it into a MipmapArray
      if (mipmaps.size() == 1) {
        return mipmaps.get(0);
      } else {
        return new MipmapArray<>(mipmaps);
      }
    } else {
      // Raster or RasterArray depending on size of list
      List<Raster<T>> rasters = new ArrayList<>(compatibleImages.size());
      for (Image<?> i : compatibleImages) {
        PixelArray r = ((Raster<?>) i).getPixelArray();
        if (i.getWidth() != displayWindow.width() || i.getHeight() != displayWindow.height()) {
          // Make a subimage
          r = SubImagePixelArray
              .createSubImageForRaster(r, displayWindow.getMinX(), displayWindow.getMinY(),
                  displayWindow.width(), displayWindow.height());
        }
        rasters.add(new Raster<>(color, r));
      }

      // If there's only one image return the raster as is, otherwise turn it into a RasterArray
      if (rasters.size() == 1) {
        return rasters.get(0);
      } else {
        return new RasterArray<>(rasters);
      }
    }
  }


  private static final int VERSION_MASK = 0xff;
  private static final int TILE_BIT = 1 << 9;
  private static final int LONG_NAME_BIT = 1 << 10;
  private static final int DEEP_DATA_BIT = 1 << 11;
  private static final int MULTIPART_BIT = 1 << 12;

  private ImageFormat readPreHeader(SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.read(in, work, 8)) {
      throw new InvalidImageException("Unexpected EOF while checking magic number");
    }

    int magicNumber = Bytes.bytesToIntLE(work);
    if (magicNumber != 20000630) {
      throw new InvalidImageException("File is not an OpenEXR image");
    }

    int version = Bytes.bytesToIntLE(work);
    if ((version & VERSION_MASK) > 2) {
      throw new UnsupportedImageFormatException(
          "OpenEXR version " + (version & VERSION_MASK) + " not supported");
    }

    // long names can be ignored, java doesn't require pre-allocation
    //        useLongNames = (version & LONG_NAME_BIT) != 0;
    boolean deep = (version & DEEP_DATA_BIT) != 0;
    boolean tiled = (version & TILE_BIT) != 0;
    boolean multipart = (version & MULTIPART_BIT) != 0;

    if (tiled) {
      if (deep || multipart) {
        throw new IOException("Single tiled image cannot be flagged as multipart or deep");
      }
      return ImageFormat.TILE;
    } else {
      if (!deep && !multipart) {
        return ImageFormat.SCANLINE;
      } else if (!deep) {
        return ImageFormat.MULTIPART;
      } else if (!multipart) {
        return ImageFormat.DEEP;
      } else {
        return ImageFormat.MULTIPART_DEEP;
      }
    }
  }

  private List<OpenEXRHeader> readHeaders(
      ImageFormat format, SeekableByteChannel in, ByteBuffer work) throws IOException {
    List<OpenEXRHeader> headers;
    if (format == ImageFormat.SCANLINE || format == ImageFormat.TILE
        || format == ImageFormat.DEEP) {
      // single-part file, don't look for a null byte at the end just read one header
      headers = Collections.singletonList(OpenEXRHeader.read(format, in, work));
    } else {
      // read all headers
      headers = readAll(in, work, (a1, a2) -> OpenEXRHeader.read(format, a1, a2));
    }

    checkSupported(format, headers);
    return headers;
  }

  private void checkSupported(ImageFormat format, List<OpenEXRHeader> headers) throws
      InvalidImageException, UnsupportedImageFormatException {
    // Some sanity checking
    if (format == ImageFormat.SCANLINE || format == ImageFormat.TILE) {
      if (headers.size() != 1) {
        throw new InvalidImageException("Multiple headers provided in non-multipart image");
      }
    }

    Set<String> headerNames = new HashSet<>();
    for (OpenEXRHeader h : headers) {
      // Header names within an exr file need to be unique, but this cannot be validated within
      // OpenEXRHeader.read() so it is done here.
      if (!headerNames.add(h.getName())) {
        throw new InvalidImageException("Header name is not unique in image: " + h.getName());
      }

      // While TileDescription is implemented to support ROUND_UP as well, it produces mipmap
      // counts and dimensions that are different than all the rest of imaJe's assumptions.
      if (h.getTileDescription() != null
          && h.getTileDescription().getRoundingMode() != RoundingMode.ROUND_DOWN) {
        throw new UnsupportedImageFormatException("ROUND UP tile mode not supported by imaJe");
      }

      // Validate that pixels are square
      if (Math.abs(1.0 - h.getPixelAspectRatio()) < 1e-16) {
        throw new UnsupportedImageFormatException(
            "Non-unit aspect ratio is unsupported: " + h.getPixelAspectRatio());
      }

      // Perform some universal channel validation as well
      for (Channel c : h.getChannels()) {
        if (c.getXSampling() != 1 || c.getYSampling() != 1) {
          throw new UnsupportedImageFormatException(
              "Non-unit x and y sampling is not supported, in channel " + c.getFullName() + " of "
                  + h.getName());
        }
      }

      // Currently deep images are not supported
      if (h.getFormat() != PartFormat.SCANLINE && h.getFormat() != PartFormat.TILE) {
        throw new UnsupportedImageFormatException(
            "Header has unsupported part format: " + h.getFormat() + " in " + h.getName());
      }

      // Make sure the compression is also currently supported (just so that this fails
      // early, even if it would fail later in a ChunkReader).
      if (!AbstractChunkReader.getSupportedCompressions().contains(h.getCompression())) {
        throw new UnsupportedImageFormatException(
            "Compression is not supported: " + h.getCompression() + " in " + h.getName());
      }

      // Ensure that the data window starts at 0,0 and that the display window is equal to
      // or contained within the data window.
      Box2Int dataWindow = h.getDataWindow();
      Box2Int displayWindow = h.getDisplayWindow();
      if (dataWindow.getMinX() != 0 || dataWindow.getMinY() != 0) {
        throw new UnsupportedImageFormatException(
            "Data window does not start at (0,0) for " + h.getName() + ": " + dataWindow);
      }
      if (displayWindow.getMinX() < dataWindow.getMinX() || displayWindow.getMaxX() > dataWindow
          .getMaxX() || displayWindow.getMinY() < dataWindow.getMinY()
          || displayWindow.getMaxY() > dataWindow.getMaxY()) {
        throw new UnsupportedImageFormatException(
            "Display window is not contained in data window for " + h.getName() + ": "
                + displayWindow);
      }
    }
  }

  private Map<OpenEXRHeader, ChunkReader> createChunkReaders(List<OpenEXRHeader> headers) throws
      UnsupportedImageFormatException {
    Map<OpenEXRHeader, ChunkReader> readers = new HashMap<>();
    for (OpenEXRHeader h : headers) {
      ChannelMapping mapping = new ChannelMapping(h);
      ChunkReader r;
      if (h.getFormat() == PartFormat.SCANLINE) {
        r = new ScanlineChunkReader(dataFactory, mapping);
      } else {
        // Assume tile, since deep formats are rejected in checkSupported()
        r = new TileChunkReader(dataFactory, mapping);
      }

      r.initialize();
      readers.put(h, r);
    }

    return readers;
  }

  private Map<OpenEXRHeader, OffsetTable> readOffsetTables(
      List<OpenEXRHeader> headers, Map<OpenEXRHeader, ChunkReader> readers, SeekableByteChannel in,
      ByteBuffer work) throws IOException {
    // Offset tables must be read in header order, hence the headers list in addition to the
    // chunk readers map which actually performs the reading.
    Map<OpenEXRHeader, OffsetTable> tables = new HashMap<>();
    for (OpenEXRHeader h : headers) {
      OffsetTable t = readers.get(h).readOffsetTable(in, work);
      tables.put(h, t);
    }

    return tables;
  }

  private Map<OpenEXRHeader, List<? extends Image<?>>> readAllChunks(
      ImageFormat format, List<OpenEXRHeader> headers, Map<OpenEXRHeader, ChunkReader> readers,
      Map<OpenEXRHeader, OffsetTable> tables, SeekableByteChannel in, ByteBuffer work) throws
      IOException {
    // For multipart images, the chunks have an additional header/part number that comes
    // before the regularly formatted chunk data.
    boolean multiPart = format == ImageFormat.MULTIPART || format == ImageFormat.MULTIPART_DEEP;

    // Calculate total number of offsets, which is equivalent to the number of blocks or chunks
    // that are included in the image.
    int blocks = 0;
    for (OffsetTable t : tables.values()) {
      blocks += t.getTotalOffsets();
    }

    // Read chunks blocks, copying them into the appropriate data locations for each data source
    if (multiPart) {
      for (int i = 0; i < blocks; i++) {
        // Must read part number first to get appropriate header
        if (!IO.read(in, work, 4)) {
          throw new InvalidImageException("Unable to read part number for multipart image");
        }
        int part = Bytes.bytesToIntLE(work);

        OpenEXRHeader h = headers.get(part);
        readers.get(h).readNextChunk(in, work);
      }
    } else {
      // Not a multipart image so there is only one header
      OpenEXRHeader h = headers.get(0);
      for (int i = 0; i < blocks; i++) {
        readers.get(h).readNextChunk(in, work);
      }
    }

    // Extract final images from the chunk readers
    Map<OpenEXRHeader, List<? extends Image<?>>> images = new HashMap<>();
    for (OpenEXRHeader h : headers) {
      images.put(h, readers.get(h).getImages());
    }
    return images;
  }
}
