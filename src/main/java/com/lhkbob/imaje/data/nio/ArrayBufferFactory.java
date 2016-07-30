package com.lhkbob.imaje.data.nio;

import java.nio.ByteBuffer;

/**
 *
 */
public class ArrayBufferFactory implements BufferFactory {
  @Override
  public ByteBuffer newByteBuffer(int length) {
    return ByteBuffer.allocate(length);
  }
}
