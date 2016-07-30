package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 */
public class DirectBufferFactory implements BufferFactory {
  private final ByteOrder byteOrder;

  public DirectBufferFactory() {
    this(ByteOrder.BIG_ENDIAN);
  }

  public DirectBufferFactory(ByteOrder byteOrder) {
    Arguments.notNull("byteOrder", byteOrder);
    this.byteOrder = byteOrder;
  }

  public static DirectBufferFactory nativeFactory() {
    return new DirectBufferFactory(ByteOrder.nativeOrder());
  }

  @Override
  public ByteBuffer newByteBuffer(int length) {
    return ByteBuffer.allocateDirect(length).order(byteOrder);
  }
}
