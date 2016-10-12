package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Predefined attribute type.
 */
public class Box2Int {
  private final int minX, minY, maxX, maxY;

  public Box2Int(int minX, int minY, int maxX, int maxY) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  public int getMinX() {
    return minX;
  }

  public int getMinY() {
    return minY;
  }

  public int getMaxX() {
    return maxX;
  }

  public int getMaxY() {
    return maxY;
  }

  public int width() {
    return maxX - minX + 1;
  }

  public int height() {
    return maxY - minY + 1;
  }

  public static Box2Int read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // 4 LE ints (16 bytes total) ordered minX, minY, maxX, maxY
    if (!IO.read(in, work, 16)) {
      throw new InvalidImageException("Unexpected EOF while reading Box2Int type");
    }

    int minX = Bytes.bytesToIntLE(work);
    int minY = Bytes.bytesToIntLE(work);
    int maxX = Bytes.bytesToIntLE(work);
    int maxY = Bytes.bytesToIntLE(work);

    Box2Int box = new Box2Int(minX, minY, maxX, maxY);

    if (maxX < minX || maxY < minY) {
      throw new InvalidImageException(
          "Maximum is less than minimum boundary for Box2Int attribute: " + box);
    }
    return box;
  }

  @Override
  public String toString() {
    return String
        .format("Box2Int(w: %d, h: %d, (%d, %d) - (%d, %d))", width(), height(), minX, minY, maxX,
            maxY);
  }
}
