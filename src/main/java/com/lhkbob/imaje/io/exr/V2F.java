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
public class V2F {
  private final float x, y;

  public V2F(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public static V2F read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.read(in, work, 8)) {
      throw new InvalidImageException("Unexpected EOF while reading V2F type");
    }

    float x = Bytes.bytesToFloatLE(work);
    float y = Bytes.bytesToFloatLE(work);
    return new V2F(x, y);
  }

  @Override
  public String toString() {
    return String.format("V2F(%.5f, %.5f)", x, y);
  }
}
