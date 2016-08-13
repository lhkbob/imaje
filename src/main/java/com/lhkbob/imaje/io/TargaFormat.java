package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.ImageBuilder;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Transforms;
import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.util.ByteOrderUtils;
import com.lhkbob.imaje.util.IOUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * See: http://www.paulbourke.net/dataformats/tga/
 * http://www.fileformat.info/format/tga/egff.htm#TGA-DMYID.2
 * http://www.dca.fee.unicamp.br/~martino/disciplinas/ea978/tgaffs.pdf
 */
public class TargaFormat implements ImageFileFormat {
  private final Data.Factory dataFactory;

  public TargaFormat() {
    this(null);
  }

  public TargaFormat(Data.Factory factory) {
    if (factory == null) {
      factory = Data.getDefaultDataFactory();
    }
    dataFactory = factory;
  }

  @Override
  public Raster<SRGB> read(SeekableByteChannel in) throws IOException {
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(WORK_BUFFER_LEN);

    Header h = readHeader(in, work);
    validateHeader(h);
    checkHeaderSupported(h);

    // Now skip idLength bytes for the imageID, which we can't do anything with
    if (h.idLength > 0) {
      if (!IOUtils.read(in, work, h.idLength)) {
        throw new InvalidImageException(
            "Channel does not have enough bytes for image ID as specified in header:" + h.idLength);
      }
      work.position(work.position() + h.idLength);
    }

    ColorMap colorMap = null;
    if (h.colorMapType != 0) {
      // Read the color map if the header says one is there, even if the image is an unmapped type
      // sometimes a color map is provided and must be skipped over to get to pixel data.
      colorMap = readColorMap(h, in, work);
    }

    if (h.requiresColorMap()) {
      if (colorMap == null) {
        throw new InvalidImageException("Required color map was not provided");
      }

      if (colorMap.elementByteCount == 2) {
        // This is 16 bit color data, so the final format will be a packed 1555 ARGB
        if (h.isRLCompressed()) {
          if (h.pixelDepth == 8) {
            return readUByteIndex16BitColorMapRLE(h, colorMap, in, work);
          } else {
            return readUShortIndex16BitColorMapRLE(h, colorMap, in, work);
          }
        } else {
          if (h.pixelDepth == 8) {
            return readUByteIndex16BitColorMap(h, colorMap, in, work);
          } else {
            return readUShortIndex16BitColorMap(h, colorMap, in, work);
          }
        }
      } else {
        // 24 or 32 bit color-mapped data
        if (h.isRLCompressed()) {
          if (h.pixelDepth == 8) {
            return readUByteIndexMultiByteColorMapRLE(h, colorMap, in, work);
          } else {
            return readUShortIndexMultiByteColorMapRLE(h, colorMap, in, work);
          }
        } else {
          if (h.pixelDepth == 8) {
            return readUByteIndexMultiByteColorMap(h, colorMap, in, work);
          } else {
            return readUShortIndexMultiByteColorMap(h, colorMap, in, work);
          }
        }
      }
    } else {
      // No color mapping
      if (h.pixelDepth == 16) {
        // 16-bit packed 1555 ARGB
        if (h.isRLCompressed()) {
          return read16BitTrueColorRLE(h, in, work);
        } else {
          return read16BitTrueColor(h, in, work);
        }
      } else {
        // 24 or 32 bit BGRA
        if (h.isRLCompressed()) {
          return readMultiByteTrueColorRLE(h, in, work);
        } else {
          return readMultiByteTrueColor(h, in, work);
        }
      }
    }
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    if (!(image instanceof Raster)) {
      throw new UnsupportedImageFormatException("Can only write 2D Rasters");
    }

    Raster<?> raster = (Raster<?>) image;
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(WORK_BUFFER_LEN);

    writeHeader(raster, out, work);

    // There is no color map to write, so now append all pixel data in
    // top-down left-to-right order, while converting to SRGB
    writePixels(raster, out, work);
  }

  private <T extends Color> void writePixels(
      Raster<T> image, SeekableByteChannel out, ByteBuffer work) throws IOException {
    ColorTransform<T, SRGB> toSRGB = Transforms.newTransform(image.getColorType(), SRGB.class);

    // A top-down left-to-right pixel loop
    T color = Color.newInstance(image.getColorType());
    for (int y = image.getHeight() - 1; y >= 0; y--) {
      for (int x = 0; x < image.getWidth(); x++) {
        double alpha = image.get(x, y, color);
        SRGB srgb = toSRGB.apply(color);

        // Always write 3 bytes for BGR
        work.put((byte) Data.UNORM8.toBits(srgb.b()));
        work.put((byte) Data.UNORM8.toBits(srgb.g()));
        work.put((byte) Data.UNORM8.toBits(srgb.r()));
        if (image.hasAlphaChannel()) {
          // Write 4th byte for alpha
          work.put((byte) Data.UNORM8.toBits(alpha));
        }

        // Push work buffer to the channel if needed
        if ((image.hasAlphaChannel() && work.remaining() < 4) || work.remaining() < 3) {
          IOUtils.write(work, out);
        }
      }
    }

    if (work.position() > 0) {
      // Finish dumping last data filled in buffer
      IOUtils.write(work, out);
    }
  }

  private void writeHeader(Raster<?> image, SeekableByteChannel out, ByteBuffer work) throws
      IOException {
    // Configure simple TGA header to specify a unmapped true-color image of 24 or 32 bits
    putUnsignedByte(work, 0); // No image ID
    putUnsignedByte(work, 0); // No color map
    putUnsignedByte(work, TYPE_TRUECOLOR);

    // Since there is no color map, the color map header bytes are all 0s
    putUnsignedShort(work, 0); // first entry index
    putUnsignedShort(work, 0); // length
    putUnsignedByte(work, 0); // entry size

    // TGA image specification fields
    putUnsignedShort(work, 0); // X origin
    putUnsignedShort(work, 0); // Y origin
    putUnsignedShort(work, image.getWidth()); // Width
    putUnsignedShort(work, image.getHeight()); // Height

    int pixelDepth = image.hasAlphaChannel() ? 32 : 24;
    putUnsignedByte(work, pixelDepth);

    // Configure image descriptor bit field. The write method assumes top-to-bottom and
    // left-to-right so that the written image is more likely to be supported.
    int attribs = ID_TOPTOBOTTOM; // Left-to-right is already default attrib bit state
    if (image.hasAlphaChannel()) {
      // If there is alpha, this will be a 32-bit image with 8 bits of alpha so attribute
      // pixel depth must be set to 8.
      attribs |= (8 << 6);
    } // Else leave it at 0
    putUnsignedByte(work, attribs);

    IOUtils.write(work, out);
  }

  private Raster<SRGB> build16BitImage(Header h, ShortData imageData) {
    ImageBuilder.OfRaster<SRGB> b = Image.newRaster(SRGB.class).width(h.width).height(h.height)
        .packedA1R5G5B5().backedBy(imageData);
    if (h.isTopToBottom()) {
      b.addDataOption(ImageBuilder.DataOption.PIXEL_TOP_TO_BOTTOM);
    }
    if (h.isRightToLeft()) {
      b.addDataOption(ImageBuilder.DataOption.PIXEL_RIGHT_TO_LEFT);
    }

    return b.build();
  }

  private Raster<SRGB> buildMultiByteImage(Header h, int numChannels, ByteData imageData) {
    ImageBuilder.OfRaster<SRGB> b = Image.newRaster(SRGB.class).width(h.width).height(h.height)
        .unorm8().backedBy(imageData);
    if (numChannels == 4) {
      b.bgra();
    } else {
      b.bgr();
    }
    if (h.isTopToBottom()) {
      b.addDataOption(ImageBuilder.DataOption.PIXEL_TOP_TO_BOTTOM);
    }
    if (h.isRightToLeft()) {
      b.addDataOption(ImageBuilder.DataOption.PIXEL_RIGHT_TO_LEFT);
    }

    return b.build();
  }

  private void checkHeaderSupported(Header h) throws UnsupportedImageFormatException {
    if (h.isBlackAndWhite()) {
      throw new UnsupportedImageFormatException("Cannot load black and white image data");
    }
    if (h.isHDRLCompressed()) {
      throw new UnsupportedImageFormatException(
          "Cannot load images with Huffman, Delta, run-length encoding");
    }
    if (h.imageType == TYPE_NO_IMAGE) {
      throw new UnsupportedImageFormatException("Cannot load an image with no image data");
    }
    if (h.getInterleavedType() != I_NOTINTERLEAVED) {
      throw new UnsupportedImageFormatException("Interleaved image data is not supported");
    }
  }

  private Raster<SRGB> read16BitTrueColor(
      Header h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.width * h.height);

    // Read all bytes and convert to LE short values before storing into the image data
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work, 2)) {
      while (offset < imageData.getLength() && work.remaining() > 2) {
        imageData.set(offset, ByteOrderUtils.bytesToShortLE(work));
        offset++;
      }
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> read16BitTrueColorRLE(
      Header h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.width * h.height);

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next 2 bytes to get the color that's repeated N times
        IOUtils.read(in, work, 2);
        short bits = ByteOrderUtils.bytesToShortLE(work);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, bits);
          offset++;
        }
      } else {
        // Read 2 * N bytes to get raw contents
        IOUtils.read(in, work, 2 * len);
        for (int i = 0; i < len; i++) {
          imageData.set(offset, ByteOrderUtils.bytesToShortLE(work));
          offset++;
        }
      }
    }

    return build16BitImage(h, imageData);
  }

  private Header readHeader(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // The header is 18 bytes long, not counting the image ID text at the end
    if (!IOUtils.read(in, work, 18)) {
      throw new InvalidImageException("Channel does not contain enough bytes to represent header");
    }

    Header h = new Header();
    // Initial header fields
    h.idLength = getUnsignedByte(work);
    h.colorMapType = getUnsignedByte(work);
    h.imageType = getUnsignedByte(work);

    // Color map header fields
    h.firstEntryIndex = getUnsignedShort(work);
    h.colorMapLength = getUnsignedShort(work);
    h.colorMapEntrySize = getUnsignedByte(work);

    // TGA image specification fields
    h.xOrigin = getUnsignedShort(work);
    h.yOrigin = getUnsignedShort(work);
    h.width = getUnsignedShort(work);
    h.height = getUnsignedShort(work);
    h.pixelDepth = getUnsignedByte(work);
    h.imageDescriptor = getUnsignedByte(work);

    return h;
  }

  private Raster<SRGB> readMultiByteTrueColor(
      Header h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    int numChannels = h.pixelDepth / 8;
    ByteData imageData = dataFactory.newByteData(h.width * h.height * numChannels);

    // Simply copy the bytes directly into the ByteData
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      if (imageData.getLength() - offset < work.remaining()) {
        // There are more bytes read into work than actually remain in the image so update the limit,
        // this can happen when there's a footer at the end of the image that we ignore.
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
      Header h, SeekableByteChannel in, ByteBuffer work) throws IOException {
    int numChannels = h.pixelDepth / 8;
    ByteData imageData = dataFactory.newByteData(h.width * h.height * numChannels);
    byte[] pixel = new byte[numChannels];

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (numChannels * len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next # channel bytes to get a color and repeat N times
        IOUtils.read(in, work, numChannels);
        work.get(pixel);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, pixel, 0, numChannels);
          offset += numChannels;
        }
      } else {
        // Read 2 * N bytes to get raw contents
        IOUtils.read(in, work, 2 * len);
        for (int i = 0; i < len; i++) {
          work.get(pixel);
          imageData.set(offset, pixel, 0, numChannels);
          offset += numChannels;
        }
      }
    }

    return buildMultiByteImage(h, numChannels, imageData);
  }

  private Raster<SRGB> readUByteIndex16BitColorMap(
      Header h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.width * h.height);

    // Read all bytes as unsigned indices into color map, lookup LE short values before storing
    // into the image data
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      while (offset < imageData.getLength() && work.hasRemaining()) {
        // Each color is 2 bytes so multiply the offset index by 2 to get the byte index instead of color index
        int index = getUByteMapIndex(work, colorMap);
        imageData.set(offset, ByteOrderUtils.bytesToShortLE(colorMap.colorMapData, index));
        offset++;
      }
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> readUByteIndex16BitColorMapRLE(
      Header h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.width * h.height);

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next byte to get an index for color map and repeat N times
        IOUtils.read(in, work);
        int index = getUByteMapIndex(work, colorMap);
        short bits = ByteOrderUtils.bytesToShortLE(colorMap.colorMapData, index);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, bits);
          offset++;
        }
      } else {
        // Read N bytes to get raw contents
        IOUtils.read(in, work, len);
        for (int i = 0; i < len; i++) {
          int index = getUByteMapIndex(work, colorMap);
          imageData.set(offset, ByteOrderUtils.bytesToShortLE(colorMap.colorMapData, index));
          offset++;
        }
      }
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> readUByteIndexMultiByteColorMap(
      Header h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ByteData imageData = dataFactory.newByteData(h.width * h.height * colorMap.elementByteCount);

    // Read all bytes as unsigned indices into color map, then copy channel count bytes from the
    // color mpa into the image data
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      while (offset < imageData.getLength() && work.hasRemaining()) {
        // Each color is specific # bytes so multiply the offset index by # to get the byte index instead of color index
        int index = getUByteMapIndex(work, colorMap);
        imageData.set(offset, colorMap.colorMapData, index, colorMap.elementByteCount);
        offset += colorMap.elementByteCount;
      }
    }

    return buildMultiByteImage(h, colorMap.elementByteCount, imageData);
  }

  private Raster<SRGB> readUByteIndexMultiByteColorMapRLE(
      Header h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ByteData imageData = dataFactory.newByteData(h.width * h.height * colorMap.elementByteCount);

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (colorMap.elementByteCount * len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next byte to get an index for color map and repeat N times
        IOUtils.read(in, work);
        int index = getUByteMapIndex(work, colorMap);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, colorMap.colorMapData, index, colorMap.elementByteCount);
          offset += colorMap.elementByteCount;
        }
      } else {
        // Read N bytes to get raw contents
        IOUtils.read(in, work, len);
        for (int i = 0; i < len; i++) {
          int index = getUByteMapIndex(work, colorMap);
          imageData.set(offset, colorMap.colorMapData, index, colorMap.elementByteCount);
          offset += colorMap.elementByteCount;
        }
      }
    }

    return buildMultiByteImage(h, colorMap.elementByteCount, imageData);
  }

  private Raster<SRGB> readUShortIndex16BitColorMap(
      Header h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.width * h.height);

    // Read all bytes as unsigned shrot indices into color map, lookup LE short values before storing
    // into the image data
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work, 2)) {
      while (offset < imageData.getLength() && work.remaining() > 2) {
        // Each color is 2 bytes so multiply the offset index by 2 to get the byte index instead of color index
        // Must also convert the 2 bytes of the pixel data into an LE index.
        int index = getUShortMapIndex(work, colorMap);
        imageData.set(offset, ByteOrderUtils.bytesToShortLE(colorMap.colorMapData, index));
        offset++;
      }
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> readUShortIndex16BitColorMapRLE(
      Header h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ShortData imageData = dataFactory.newShortData(h.width * h.height);

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next 2 bytes to get an index for color map and repeat N times
        IOUtils.read(in, work, 2);
        int index = getUShortMapIndex(work, colorMap);
        short bits = ByteOrderUtils.bytesToShortLE(colorMap.colorMapData, index);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, bits);
          offset++;
        }
      } else {
        // Read 2 * N bytes to get raw contents
        IOUtils.read(in, work, 2 * len);
        for (int i = 0; i < len; i++) {
          int index = getUShortMapIndex(work, colorMap);
          imageData.set(offset, ByteOrderUtils.bytesToShortLE(colorMap.colorMapData, index));
          offset++;
        }
      }
    }

    return build16BitImage(h, imageData);
  }

  private Raster<SRGB> readUShortIndexMultiByteColorMap(
      Header h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ByteData imageData = dataFactory.newByteData(h.width * h.height * colorMap.elementByteCount);

    // Read all bytes as unsigned short indices into color map, then copy channel count bytes from the
    // color mpa into the image data
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work, 2)) {
      while (offset < imageData.getLength() && work.remaining() > 2) {
        // Each color is specific # bytes so multiply the offset index by # to get the byte index instead of color index
        int index = getUShortMapIndex(work, colorMap);
        imageData.set(offset, colorMap.colorMapData, index, colorMap.elementByteCount);
        offset += colorMap.elementByteCount;
      }
    }

    return buildMultiByteImage(h, colorMap.elementByteCount, imageData);
  }

  private Raster<SRGB> readUShortIndexMultiByteColorMapRLE(
      Header h, ColorMap colorMap, SeekableByteChannel in, ByteBuffer work) throws IOException {
    ByteData imageData = dataFactory.newByteData(h.width * h.height * colorMap.elementByteCount);

    // Read bytes, checking for packet type and then reading subsequent value(s) for map indices
    long offset = 0;
    while (offset < imageData.getLength() && IOUtils.read(in, work)) {
      byte p = work.get();
      int len = getPacketLength(p);
      if (colorMap.elementByteCount * len + offset > imageData.getLength()) {
        throw new InvalidImageException("Run length exceeds image size");
      }

      if (isRLEPacket(p)) {
        // Read next 2 bytes to get an index for color map and repeat N times
        IOUtils.read(in, work, 2);
        int index = getUShortMapIndex(work, colorMap);

        for (int i = 0; i < len; i++) {
          imageData.set(offset, colorMap.colorMapData, index, colorMap.elementByteCount);
          offset += colorMap.elementByteCount;
        }
      } else {
        // Read 2 * N bytes to get raw contents
        IOUtils.read(in, work, 2 * len);
        for (int i = 0; i < len; i++) {
          int index = getUShortMapIndex(work, colorMap);
          imageData.set(offset, colorMap.colorMapData, index, colorMap.elementByteCount);
          offset += colorMap.elementByteCount;
        }
      }
    }

    return buildMultiByteImage(h, colorMap.elementByteCount, imageData);
  }

  private void validateHeader(Header h) throws InvalidImageException,
      UnsupportedImageFormatException {
    if (h.idLength < 0 || h.idLength > 255) {
      throw new InvalidImageException("Bad idLength value: " + h.idLength);
    }
    if (h.colorMapType != 0 && h.colorMapType != 1) {
      throw new InvalidImageException("Bad color map type: " + h.colorMapType);
    }

    switch (h.imageType) {
    case TYPE_BLACKWHITE:
    case TYPE_COLORMAP:
    case TYPE_HDRL_COLORMAP_QUAD:
    case TYPE_HDRL_COLORMAP:
    case TYPE_NO_IMAGE:
    case TYPE_RL_BLACKWHITE:
    case TYPE_RL_COLORMAP:
    case TYPE_RL_TRUECOLOR:
    case TYPE_TRUECOLOR:
      break;
    default:
      throw new InvalidImageException("Bad image type: " + h.imageType);
    }

    if (h.requiresColorMap()) {
      if (h.firstEntryIndex < 0) {
        throw new InvalidImageException(
            "Bad first entry index for a color map: " + h.firstEntryIndex);
      }
      if (h.colorMapLength < 0) {
        throw new InvalidImageException("Bad number of color map entries: " + h.colorMapLength);
      }
      if (!h.isBlackAndWhite()) {
        if (h.colorMapEntrySize != 16 && h.colorMapEntrySize != 24 && h.colorMapEntrySize != 32) {
          throw new UnsupportedImageFormatException(
              "Unsupported color map entry size: " + h.colorMapEntrySize);
        }
      }
      // In this case "pixel depth" refers to bits in a color map index
      if (h.pixelDepth != 8 && h.pixelDepth != 16) {
        throw new InvalidImageException("Pixel depth doesn't have a valid value: " + h.pixelDepth);
      }
      if (h.colorMapType == 0) {
        throw new InvalidImageException("Image type expects a color map, but one is not specified");
      }
    } else if (!h.isBlackAndWhite()) {
      switch (h.pixelDepth) {
      case 16:
        if (h.getAttributeBitsPerPixel() != 1) {
          throw new InvalidImageException(
              "Bad attribs pixel count, must be 1 for 16 bit colors: " + h
                  .getAttributeBitsPerPixel());
        }
        break;
      case 24:
        if (h.getAttributeBitsPerPixel() != 0) {
          throw new InvalidImageException(
              "Bad attribs pixel count, must be 0 for 24 bit colors: " + h
                  .getAttributeBitsPerPixel());
        }
        break;
      case 32:
        if (h.getAttributeBitsPerPixel() != 8) {
          throw new InvalidImageException(
              "Bad attribs pixel count, must be 8 for 32 bit colors: " + h
                  .getAttributeBitsPerPixel());
        }
        break;
      default:
        throw new UnsupportedImageFormatException("Unsupported pixel depth: " + h.pixelDepth);
      }
    }

    // Ignore the x and y origins of the image (pertain only to screen location)
    if (h.width < 0) {
      throw new InvalidImageException("Bad width, must be positive: " + h.width);
    }
    if (h.height < 0) {
      throw new InvalidImageException("Bad height, must be positive: " + h.height);
    }
  }

  private static int getPacketLength(byte p) {
    return 0x7f & p;
  }

  private static int getUByteMapIndex(ByteBuffer data, ColorMap cm) {
    return cm.elementByteCount * (getUnsignedByte(data) - cm.startIndex);
  }

  private static int getUShortMapIndex(ByteBuffer data, ColorMap cm) {
    return cm.elementByteCount * (getUnsignedShort(data) - cm.startIndex);
  }

  private static void putUnsignedByte(ByteBuffer work, int value) {
    work.put((byte) (0xff & value));
  }

  private static void putUnsignedShort(ByteBuffer work, int value) {
    ByteOrderUtils.shortToBytesBE((short) (0xffff & value), work);
  }

  private static int getUnsignedByte(ByteBuffer work) {
    return 0xff & work.get();
  }

  private static int getUnsignedShort(ByteBuffer work) {
    return 0xffff & ByteOrderUtils.bytesToShortBE(work);
  }

  private static boolean isRLEPacket(byte p) {
    return (0x80 & p) > 0;
  }

  // TODO maybe make this a public function in IOUtils?
  private static void readAll(SeekableByteChannel in, ByteBuffer work, byte[] data) throws
      IOException {
    if (data.length < work.capacity()) {
      // Can use the basic read functionality already in IOUtils
      IOUtils.read(in, work, data.length);
      work.get(data);
    } else {
      // The requested block of data is larger than the work buffer so it must be incrementally filled
      int filled = 0;
      while (filled < data.length) {
        IOUtils.read(in, work);
        int consumed = Math.min(work.remaining(), data.length - filled);
        work.get(data, filled, consumed);
        filled += consumed;
      }
    }
  }

  private static ColorMap readColorMap(Header h, SeekableByteChannel in, ByteBuffer work) throws
      IOException {
    ColorMap cm = new ColorMap(h);
    readAll(in, work, cm.colorMapData);
    return cm;
  }

  /*
   * Field image descriptor bitfield values definitions
   */
  private static final int ID_ATTRIBPERPIXEL = 0xF;
  private static final int ID_INTERLEAVE = 0xC0;
  private static final int ID_RIGHTTOLEFT = 0x10;
  private static final int ID_TOPTOBOTTOM = 0x20;
  /*
   * Field image descriptor / interleave values
   */
  private static final int I_FOURWAY = 2;
  private static final int I_NOTINTERLEAVED = 0;
  private static final int I_TWOWAY = 1;
  /*
   * Set of possible image types in TGA file
   */
  // Uncompressed black and white image
  private static final int TYPE_BLACKWHITE = 3;
  // Uncompressed color mapped image
  private static final int TYPE_COLORMAP = 1;
  // Compressed color mapped data with Huffman, Delta, and runlength encoding
  private static final int TYPE_HDRL_COLORMAP = 33;
  // Compressed color mapped data using Huffman, Delta, and runlength encoding, 4-pass quadtree processing.
  private static final int TYPE_HDRL_COLORMAP_QUAD = 32;
  // No image data
  private static final int TYPE_NO_IMAGE = 0;
  // Compressed black and white image via runlength encoding
  private static final int TYPE_RL_BLACKWHITE = 11;
  // Compressed color mapped image via runlength encoding
  private static final int TYPE_RL_COLORMAP = 9;
  // Compressed true color image via runlength encoding
  private static final int TYPE_RL_TRUECOLOR = 10;
  // Uncompressed true color image
  private static final int TYPE_TRUECOLOR = 2;
  private static final int WORK_BUFFER_LEN = 4096;

  private static class ColorMap {
    final byte[] colorMapData;
    final int elementByteCount;
    final int numElements;
    final int startIndex;

    ColorMap(Header h) {
      startIndex = h.firstEntryIndex;
      elementByteCount = h.colorMapEntrySize >> 8;
      numElements = h.colorMapLength;
      colorMapData = new byte[numElements * elementByteCount];
    }
  }

  private static class Header {
    int colorMapEntrySize; // Color map field 6 - LE ushort
    int colorMapLength; // Color map field 5 - LE ushort
    int colorMapType; // Initial image data field 2 - byte value
    int firstEntryIndex; // Color map field 4 - LE ushort
    int height; // Image spec field 10 - LE ushort
    int idLength; // Initial image data field 1 - byte value
    int imageDescriptor; // Image spec field 12 - byte
    int imageType; // Initial image data field 3 - byte value
    int pixelDepth; // Image spec field 11 - byte
    int width; // Image spec field 9 - LE ushort
    int xOrigin; // Image spec field 7 - LE ushort
    int yOrigin; // Image spec field 8 - LE ushort

    int getAttributeBitsPerPixel() {
      return imageDescriptor & ID_ATTRIBPERPIXEL;
    }

    int getInterleavedType() {
      return (imageDescriptor & ID_INTERLEAVE) >> 6;
    }

    boolean isBlackAndWhite() {
      switch (imageType) {
      case TYPE_BLACKWHITE:
      case TYPE_RL_BLACKWHITE:
        return true;
      default:
        return false;
      }
    }

    boolean isHDRLCompressed() {
      switch (imageType) {
      case TYPE_HDRL_COLORMAP:
      case TYPE_HDRL_COLORMAP_QUAD:
        return true;
      default:
        return false;
      }
    }

    boolean isRLCompressed() {
      switch (imageType) {
      case TYPE_RL_BLACKWHITE:
      case TYPE_RL_COLORMAP:
      case TYPE_RL_TRUECOLOR:
        return true;
      default:
        return false;
      }
    }

    boolean isRightToLeft() {
      return ((imageDescriptor & ID_RIGHTTOLEFT) != 0);
    }

    boolean isTopToBottom() {
      return ((imageDescriptor & ID_TOPTOBOTTOM) != 0);
    }

    boolean requiresColorMap() {
      switch (imageType) {
      case TYPE_HDRL_COLORMAP:
      case TYPE_HDRL_COLORMAP_QUAD:
      case TYPE_COLORMAP:
      case TYPE_RL_COLORMAP:
        return true;
      default:
        return false;
      }
    }
  }
}
