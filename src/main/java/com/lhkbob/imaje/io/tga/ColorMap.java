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
