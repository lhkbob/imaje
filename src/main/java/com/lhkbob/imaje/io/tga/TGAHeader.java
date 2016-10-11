package com.lhkbob.imaje.io.tga;

import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;

import static com.lhkbob.imaje.io.tga.TGAFormat.getUnsignedByte;
import static com.lhkbob.imaje.io.tga.TGAFormat.getUnsignedShort;
import static com.lhkbob.imaje.io.tga.TGAFormat.putUnsignedByte;
import static com.lhkbob.imaje.io.tga.TGAFormat.putUnsignedShort;

/**
 *
 */
public class TGAHeader {
  public static final int ID_ATTRIBPERPIXEL = 0xF;
  public static final int ID_INTERLEAVE = 0xC0;
  public static final int ID_RIGHTTOLEFT = 0x10;
  public static final int ID_TOPTOBOTTOM = 0x20;

  private int colorMapEntrySize; // Color map field 6 - LE ushort
  private int colorMapLength; // Color map field 5 - LE ushort
  private int colorMapType; // Initial image data field 2 - byte value
  private int firstEntryIndex; // Color map field 4 - LE ushort
  private int height; // Image spec field 10 - LE ushort
  private String id; // Initial image data field 1 - byte value, length
  private int imageDescriptor; // Image spec field 12 - byte
  private ImageType imageType; // Initial image data field 3 - byte value
  private int pixelDepth; // Image spec field 11 - byte
  private int width; // Image spec field 9 - LE ushort
  private int xOrigin; // Image spec field 7 - LE ushort
  private int yOrigin; // Image spec field 8 - LE ushort

  public static TGAHeader read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // The header is 18 bytes long, not counting the image ID text at the end
    if (!IO.read(in, work, 18)) {
      throw new InvalidImageException("Channel does not contain enough bytes to represent header");
    }

    TGAHeader h = new TGAHeader();
    // Initial header fields
    int idLength = getUnsignedByte(work);
    if (idLength < 0 || idLength > 255) {
      throw new InvalidImageException("Bad idLength value: " + idLength);
    }

    h.colorMapType = getUnsignedByte(work);

    int imageType = getUnsignedByte(work);
    h.imageType = ImageType.fromTypeID(imageType);
    if (h.imageType == null) {
      throw new InvalidImageException("Bad image type: " + imageType);
    }

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

    // Now skip idLength bytes for the imageID, which we can't do anything with
    if (idLength > 0) {
      if (!IO.read(in, work, idLength)) {
        throw new InvalidImageException(
            "Channel does not have enough bytes for image ID as specified in header:" + idLength);
      }
      byte[] idBytes = new byte[idLength];
      work.get(idBytes);
      h.id = new String(idBytes, Charset.forName("ASCII"));
    } else {
      h.id = "";
    }

    h.validateHeader();
    return h;
  }

  public void write(SeekableByteChannel out, ByteBuffer work) throws IOException {
    putUnsignedByte(work, getIDLength()); // Image ID length
    putUnsignedByte(work, colorMapType); // Color map type
    putUnsignedByte(work, imageType.getTypeID()); // Image type

    // Color map definition
    putUnsignedShort(work, firstEntryIndex); // first entry index
    putUnsignedShort(work, colorMapLength); // length
    putUnsignedByte(work, colorMapEntrySize); // entry size

    // TGA image specification fields
    putUnsignedShort(work, xOrigin); // X origin
    putUnsignedShort(work, yOrigin); // Y origin
    putUnsignedShort(work, width); // Width
    putUnsignedShort(work, height); // Height

    putUnsignedByte(work, pixelDepth);
    putUnsignedByte(work, imageDescriptor);

    IO.write(work, out);
  }

  private void validateHeader() throws InvalidImageException,
      UnsupportedImageFormatException {
    if (colorMapType != 0 && colorMapType != 1) {
      throw new InvalidImageException("Bad color map type: " + colorMapType);
    }

    if (imageType.requiresColorMap()) {
      if (firstEntryIndex < 0) {
        throw new InvalidImageException(
            "Bad first entry index for a color map: " + firstEntryIndex);
      }
      if (colorMapLength < 0) {
        throw new InvalidImageException("Bad number of color map entries: " + colorMapLength);
      }
      if (!imageType.isBlackAndWhite()) {
        if (colorMapEntrySize != 16 && colorMapEntrySize != 24 && colorMapEntrySize != 32) {
          throw new UnsupportedImageFormatException(
              "Unsupported color map entry size: " + colorMapEntrySize);
        }
      }
      // In this case "pixel depth" refers to bits in a color map index
      if (pixelDepth != 8 && pixelDepth != 16) {
        throw new InvalidImageException("Pixel depth doesn't have a valid value: " + pixelDepth);
      }
      if (colorMapType == 0) {
        throw new InvalidImageException("Image type expects a color map, but one is not specified");
      }
    } else if (!imageType.isBlackAndWhite()) {
      switch (pixelDepth) {
      case 16:
        if (getAttributeBitsPerPixel() != 1) {
          throw new InvalidImageException(
              "Bad attribs pixel count, must be 1 for 16 bit colors: " + getAttributeBitsPerPixel());
        }
        break;
      case 24:
        if (getAttributeBitsPerPixel() != 0) {
          throw new InvalidImageException(
              "Bad attribs pixel count, must be 0 for 24 bit colors: " + getAttributeBitsPerPixel());
        }
        break;
      case 32:
        if (getAttributeBitsPerPixel() != 8) {
          throw new InvalidImageException(
              "Bad attribs pixel count, must be 8 for 32 bit colors: " + getAttributeBitsPerPixel());
        }
        break;
      default:
        throw new UnsupportedImageFormatException("Unsupported pixel depth: " + pixelDepth);
      }
    }

    // Ignore the x and y origins of the image (pertain only to screen location)
    if (width < 0) {
      throw new InvalidImageException("Bad width, must be positive: " + width);
    }
    if (height < 0) {
      throw new InvalidImageException("Bad height, must be positive: " + height);
    }
  }

  public String getID() {
    return id;
  }

  public void setID(String id) {
    this.id = id;
  }

  public int getIDLength() {
    if (id != null)
      return id.length();
    else
      return 0;
  }

  public int getColorMapFirstEntry() {
    return firstEntryIndex;
  }

  public void setColorMapFirstEntry(int index) {
    firstEntryIndex = index;
  }

  public boolean hasColorMap() {
    return colorMapType != 0;
  }

  public void setHasColorMap(boolean hasColorMap) {
    colorMapType = (hasColorMap ? 1 : 0);
  }

  public int getXOrigin() {
    return xOrigin;
  }

  public void setXOrigin(int x) {
    xOrigin = x;
  }

  public int getYOrigin() {
    return yOrigin;
  }

  public void setYOrigin(int y) {
    yOrigin = y;
  }

  public int getPixelDepth() {
    return pixelDepth;
  }

  public void setPixelDepth(int depth) {
    pixelDepth = depth;
  }

  public int getColorMapEntrySize() {
    return colorMapEntrySize;
  }

  public void setColorMapEntrySize(int size) {
    colorMapEntrySize = size;
  }

  public int getColorMapLength() {
    return colorMapLength;
  }

  public void setColorMapLength(int length) {
    colorMapLength = length;
  }

  public ImageType getImageType() {
    return imageType;
  }

  public void setImageType(ImageType type) {
    imageType = type;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getImageDescriptor() {
    return imageDescriptor;
  }

  public void setImageDescriptor(int bits) {
    imageDescriptor = bits;
  }

  public int getAttributeBitsPerPixel() {
    return imageDescriptor & ID_ATTRIBPERPIXEL;
  }

  public void setAttributeBitsPerPixel(int bits) {
    int maskedDesc = imageDescriptor & ~ID_ATTRIBPERPIXEL;
    imageDescriptor = maskedDesc | (bits & ID_ATTRIBPERPIXEL);
  }

  public InterleaveType getInterleavedType() {
    return InterleaveType.values()[(imageDescriptor & ID_INTERLEAVE) >> 6];
  }

  public void setInterleavedType(InterleaveType type) {
    int iMask = type.ordinal() << 6;
    int maskedDesc = imageDescriptor & ~ID_INTERLEAVE;
    imageDescriptor = maskedDesc | iMask;
  }

  public boolean isRightToLeft() {
    return ((imageDescriptor & ID_RIGHTTOLEFT) != 0);
  }

  public void setRightToLeft(boolean rightToLeft) {
    int rlMask = (rightToLeft ? ID_RIGHTTOLEFT : 0);
    int maskedDesc = imageDescriptor & ~ID_RIGHTTOLEFT;
    imageDescriptor = maskedDesc | rlMask;
  }

  public boolean isTopToBottom() {
    return ((imageDescriptor & ID_TOPTOBOTTOM) != 0);
  }

  public void setTopToBottom(boolean topToBottom) {
    int tbMask = (topToBottom ? ID_TOPTOBOTTOM : 0);
    int maskedDesc = imageDescriptor & ~ID_TOPTOBOTTOM;
    imageDescriptor = maskedDesc | tbMask;
  }
}
