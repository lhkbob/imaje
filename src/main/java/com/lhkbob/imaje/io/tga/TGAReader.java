package com.lhkbob.imaje.io.tga;

import com.lhkbob.imaje.ImageBuilder;
import com.lhkbob.imaje.Images;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.ImageFileReader;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public class TGAReader implements ImageFileReader {
  private final Data.Factory dataFactory;

  public TGAReader(@Arguments.Nullable Data.Factory factory) {
    if (factory == null) {
      dataFactory = Data.getDefaultDataFactory();
    } else {
      dataFactory = factory;
    }
  }

  @Override
  public Raster<SRGB> read(SeekableByteChannel in) throws IOException {
    ByteBuffer work = IO.createWorkBufferForReading();

    TGAHeader h = TGAHeader.read(in, work);
    checkHeaderSupported(h);

    ColorMap colorMap = null;
    if (h.hasColorMap()) {
      // Read the color map if the header says one is there, even if the image is an unmapped type
      // sometimes a color map is provided and must be skipped over to get to pixel data.
      colorMap = ColorMap.read(h, in, work);
    }

    if (h.getImageType().requiresColorMap()) {
      if (colorMap == null) {
        throw new InvalidImageException("Required color map was not provided");
      }

      if (colorMap.getElementByteSize() == 2) {
        // This is 16 bit color data, so the final format will be a packed 1555 ARGB
        if (h.getImageType().isRunLengthEncoded()) {
          return read16BitColorMapRLE(h, colorMap, in, work);
        } else {
          return read16BitColorMap(h, colorMap, in, work);
        }
      } else {
        // 24 or 32 bit color-mapped data
        if (h.getImageType().isRunLengthEncoded()) {
          return readMultiByteColorMapRLE(h, colorMap, in, work);
        } else {
          return readMultiByteColorMap(h, colorMap, in, work);
        }
      }
    } else {
      // No color mapping
      if (h.getPixelDepth() == 16) {
        // 16-bit packed 1555 ARGB
        if (h.getImageType().isRunLengthEncoded()) {
          return read16BitTrueColorRLE(h, in, work);
        } else {
          return read16BitTrueColor(h, in, work);
        }
      } else {
        // 24 or 32 bit BGRA
        if (h.getImageType().isRunLengthEncoded()) {
          return readMultiByteTrueColorRLE(h, in, work);
        } else {
          return readMultiByteTrueColor(h, in, work);
        }
      }
    }
  }

  private void checkHeaderSupported(TGAHeader h) throws UnsupportedImageFormatException {
    if (h.getImageType().isBlackAndWhite()) {
      throw new UnsupportedImageFormatException("Cannot load black and white image data");
    }
    if (h.getImageType().isHuffmanDeltaCompressed()) {
      throw new UnsupportedImageFormatException(
          "Cannot load images with Huffman, Delta, run-length encoding");
    }
    if (h.getImageType() == ImageType.NO_IMAGE) {
      throw new UnsupportedImageFormatException("Cannot load an image with no image data");
    }
    if (h.getInterleavedType() != InterleaveType.NONE) {
      throw new UnsupportedImageFormatException("Interleaved image data is not supported");
    }
  }

  private Raster<SRGB> build16BitImage(TGAHeader h, ShortData imageData) {
    ImageBuilder.OfRaster<SRGB> b = Images.newRaster(SRGB.class).width(h.getWidth())
        .height(h.getHeight()).packedA1R5G5B5().existingData(imageData);
    if (h.isTopToBottom()) {
      b.pixelsTopToBottom();
    }
    if (h.isRightToLeft()) {
      b.pixelsRightToLeft();
    }

    return b.build();
  }

  private Raster<SRGB> buildMultiByteImage(TGAHeader h, int numChannels, ByteData imageData) {
    ImageBuilder.OfRaster<SRGB> b = Images.newRaster(SRGB.class).width(h.getWidth())
        .height(h.getHeight()).unorm8().existingData(imageData);
    if (numChannels == 4) {
      b.bgra();
    } else {
      b.bgr();
    }
    if (h.isTopToBottom()) {
      b.pixelsTopToBottom();
    }
    if (h.isRightToLeft()) {
      b.pixelsRightToLeft();
    }

    return b.build();
  }


  private Raster<SRGB> read16BitTrueColor(
      TGAHeader h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.getWidth() * h.getHeight());

    // Read all bytes and convert to LE short values before storing into the image data
    long offset = 0;
    while (offset < imageData.getLength() && IO.read(in, work, 2)) {
      while (offset < imageData.getLength() && work.remaining() > 2) {
        imageData.set(offset, Bytes.bytesToShortLE(work));
        offset++;
      }
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> read16BitTrueColorRLE(
      TGAHeader h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.getWidth() * h.getHeight());

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IO.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next 2 bytes to get the color that's repeated N times
        IO.read(in, work, 2);
        short bits = Bytes.bytesToShortLE(work);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, bits);
          offset++;
        }
      } else {
        // Read 2 * N bytes to get raw contents
        IO.read(in, work, 2 * len);
        for (int i = 0; i < len; i++) {
          imageData.set(offset, Bytes.bytesToShortLE(work));
          offset++;
        }
      }
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> readMultiByteTrueColor(
      TGAHeader h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    int numChannels = h.getPixelDepth() / 8;
    ByteData imageData = dataFactory.newByteData(h.getWidth() * h.getHeight() * numChannels);

    // Simply copy the bytes directly into the ByteData
    long offset = 0;
    while (offset < imageData.getLength() && IO.read(in, work)) {
      if (imageData.getLength() - offset < work.remaining()) {
        // There are more bytes read into work than actually remain in the image so update the
        // limit, this can happen when there's a footer at the end of the image that we ignore.
        work.limit(work.position() + Math.toIntExact(imageData.getLength() - offset));
      }
      // Update the image data and advance offset
      int transferred = work.remaining();
      imageData.set(offset, work);
      offset += transferred;
    }

    return buildMultiByteImage(h, numChannels, imageData);
  }

  private Raster<SRGB> readMultiByteTrueColorRLE(
      TGAHeader h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    int numChannels = h.getPixelDepth() / 8;
    ByteData imageData = dataFactory.newByteData(h.getWidth() * h.getHeight() * numChannels);
    byte[] pixel = new byte[numChannels];

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IO.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (numChannels * len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next # channel bytes to get a color and repeat N times
        IO.read(in, work, numChannels);
        work.get(pixel);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, pixel, 0, numChannels);
          offset += numChannels;
        }
      } else {
        // Read numChannels * N bytes to get raw contents
        IO.read(in, work, numChannels * len);
        for (int i = 0; i < len; i++) {
          work.get(pixel);
          imageData.set(offset, pixel, 0, numChannels);
          offset += numChannels;
        }
      }
    }

    return buildMultiByteImage(h, numChannels, imageData);
  }

  private Raster<SRGB> read16BitColorMap(
      TGAHeader h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.getWidth() * h.getHeight());

    int mapIndexByteSize = h.getPixelDepth() / 8;

    // Read all bytes as unsigned shrot indices into color map, lookup LE short values before
    // storing into the image data
    long offset = 0;
    while (offset < imageData.getLength() && IO.read(in, work, mapIndexByteSize)) {
      while (offset < imageData.getLength() && work.remaining() > mapIndexByteSize) {
        // Each color is 2 bytes so multiply the offset index by 2 to get the byte index instead of
        // color index. Must also convert the 2 bytes of the pixel data into an LE index.
        int index = colorMap.getNextDataIndex(work);
        imageData.set(offset, Bytes.bytesToShortLE(colorMap.getColorMapData(), index));
        offset++;
      }
    }

    if (offset != imageData.getLength()) {
      throw new InvalidImageException(
          "Incomplete image data in file, EOF reached before buffer was filled");
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> read16BitColorMapRLE(
      TGAHeader h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.getWidth() * h.getHeight());

    int mapIndexByteSize = h.getPixelDepth() / 8;

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IO.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next N bytes to get an index for color map and repeat N times
        if (!IO.read(in, work, mapIndexByteSize)) {
          throw new InvalidImageException("Unable to read color map index for RLE packet");
        }

        int index = colorMap.getNextDataIndex(work);
        short bits = Bytes.bytesToShortLE(colorMap.getColorMapData(), index);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, bits);
          offset++;
        }
      } else {
        // Read N * len bytes to get raw contents
        if (!IO.read(in, work, mapIndexByteSize * len)) {
          throw new InvalidImageException("Unable to read all bytes in non-RLE packet");
        }

        for (int i = 0; i < len; i++) {
          int index = colorMap.getNextDataIndex(work);
          imageData.set(offset, Bytes.bytesToShortLE(colorMap.getColorMapData(), index));
          offset++;
        }
      }
    }

    if (offset != imageData.getLength()) {
      throw new InvalidImageException(
          "Incomplete image data in file, EOF reached before buffer was filled");
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> readMultiByteColorMap(
      TGAHeader h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ByteData imageData = dataFactory
        .newByteData(h.getWidth() * h.getHeight() * colorMap.getElementByteSize());

    int mapIndexByteSize = h.getPixelDepth() / 8;

    // Read all bytes as unsigned short indices into color map, then copy channel count bytes from
    // the color mpa into the image data
    long offset = 0;
    while (offset < imageData.getLength() && IO.read(in, work, mapIndexByteSize)) {
      while (offset < imageData.getLength() && work.remaining() > mapIndexByteSize) {
        // Each color is specific # bytes so multiply the offset index by # to get the byte index
        // instead of color index
        int index = colorMap.getNextDataIndex(work);
        imageData.set(offset, colorMap.getColorMapData(), index, colorMap.getElementByteSize());
        offset += colorMap.getElementByteSize();
      }
    }

    if (offset != imageData.getLength()) {
      throw new InvalidImageException(
          "Incomplete image data in file, EOF reached before buffer was filled");
    }

    return buildMultiByteImage(h, colorMap.getElementByteSize(), imageData);
  }

  private Raster<SRGB> readMultiByteColorMapRLE(
      TGAHeader h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ByteData imageData = dataFactory
        .newByteData(h.getWidth() * h.getHeight() * colorMap.getElementByteSize());

    int mapIndexByteSize = h.getPixelDepth() / 8;

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IO.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (colorMap.getElementByteSize() * len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next N bytes to get an index for color map and repeat N times
        if (!IO.read(in, work, mapIndexByteSize)) {
          throw new InvalidImageException("Unable to read color map index for RLE packet");
        }
        int index = colorMap.getNextDataIndex(work);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, colorMap.getColorMapData(), index, colorMap.getElementByteSize());
          offset += colorMap.getElementByteSize();
        }
      } else {
        // Read N * len bytes to get raw contents
        if (!IO.read(in, work, mapIndexByteSize * len)) {
          throw new InvalidImageException("Unable to read all bytes in non-RLE packet");
        }
        for (int i = 0; i < len; i++) {
          int index = colorMap.getNextDataIndex(work);
          imageData.set(offset, colorMap.getColorMapData(), index, colorMap.getElementByteSize());
          offset += colorMap.getElementByteSize();
        }
      }
    }

    if (offset != imageData.getLength()) {
      throw new InvalidImageException(
          "Incomplete image data in file, EOF reached before buffer was filled");
    }

    return buildMultiByteImage(h, colorMap.getElementByteSize(), imageData);
  }

  private static int getPacketLength(byte p) {
    return 0x7f & p;
  }


  private static boolean isRLEPacket(byte p) {
    return (0x80 & p) > 0;
  }
}
