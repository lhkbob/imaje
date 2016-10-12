package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.layout.PixelFormat;

/**
 *
 */
public enum ChannelFormat {
  UINT(PixelFormat.Type.UINT, 4), HALF(PixelFormat.Type.SFLOAT, 2), FLOAT(PixelFormat.Type.SFLOAT, 4);

  private final int bytes;
  private final PixelFormat.Type type;

  ChannelFormat(PixelFormat.Type type, int bytes) {
    this.type = type;
    this.bytes = bytes;
  }

  public int getByteCount() {
    return bytes;
  }

  public int getBits() {
    return bytes * Byte.SIZE;
  }

  public PixelFormat.Type getPixelType() {
    return type;
  }
}
