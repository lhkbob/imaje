package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;

/**
 *
 */
public class Attribute {
  private final String name;
  private final String type;
  private final int size; // in bytes
  // this will be of particular classes if type is known or null if type was unknown
  private final Object value;

  public Attribute(String name, String type, int size, Object value) {
    this.name = name;
    this.type = type;
    this.size = size;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public int getSize() {
    return size;
  }

  public Object getValue() {
    return value;
  }

  public static Attribute read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    String name = TypeReader.readNullTerminatedString(in, work);
    String type = TypeReader.readNullTerminatedString(in, work).toLowerCase();

    if (!IO.read(in, work, 4)) {
      throw new InvalidImageException("Unexpected EOF while reading attribute size");
    }
    int size = Bytes.bytesToIntLE(work);

    Object value;
    switch (type) {
    case "box2i":
      if (size != 16) {
        throw new InvalidImageException(
            "Unexpected size (" + size + ") of type (" + type + ") for attribute (" + name + ")");
      }

      value = Box2Int.read(in, work);
      break;
    case "compression":
      if (size != 1) {
        throw new InvalidImageException(
            "Unexpected size (" + size + ") of type (" + type + ") for attribute (" + name + ")");
      }

      value = Compression.read(in, work);
      break;
    case "chlist":
      // don't bother checking size for null terminated attribute
      if (IO.remaining(in, work) < size) {
        throw new InvalidImageException(
            "Not enough bytes remain in file (has " + (in.size() - in.position())
                + ") to read channel list of size " + size);
      }
      value = TypeReader.readAll(in, work, Channel::read);
      break;
    case "lineorder":
      if (size != 1) {
        throw new InvalidImageException(
            "Unexpected size (" + size + ") of type (" + type + ") for attribute (" + name + ")");
      }

      value = LineOrder.read(in, work);
      break;
    case "tiledesc":
      if (size != 9) {
        throw new InvalidImageException(
            "Unexpected size (" + size + ") of type (" + type + ") for attribute (" + name + ")");
      }
      value = TileDescription.read(in, work);
      break;
    case "string":
      value = readString(in, work, size);
      break;
    case "v2f":
      if (size != 8) {
        throw new InvalidImageException(
            "Unexpected size (" + size + ") of type (" + type + ") for attribute (" + name + ")");
      }

      value = V2F.read(in, work);
      break;
    case "float":
      if (size != 4) {
        throw new IOException(
            "Unexpected size (" + size + ") of type (" + type + ") for attribute (" + name + ")");
      }

      IO.read(in, work, 4);
      value = Bytes.bytesToFloatLE(work);
      break;
    case "int":
      if (size != 4) {
        throw new IOException(
            "Unexpected size (" + size + ") of type (" + type + ") for attribute (" + name + ")");
      }
      IO.read(in, work, 4);
      value = Bytes.bytesToIntLE(work);
      break;
    default:
      // Skip size bytes in the channel
      IO.skip(in, work, size);
      value = null;
      break;
    }

    return new Attribute(name, type, size, value);
  }

  private static String readString(SeekableByteChannel in, ByteBuffer work, int length) throws
      IOException {
    if (!IO.read(in, work, length)) {
      throw new InvalidImageException(
          "Not enough bytes remaining in file to read string of length " + length);
    }
    byte[] b = new byte[length];
    work.get(b);
    return new String(b, Charset.forName("ASCII"));
  }


}
