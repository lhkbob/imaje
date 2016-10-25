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
