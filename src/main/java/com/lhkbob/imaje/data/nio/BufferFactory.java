package com.lhkbob.imaje.data.nio;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 *
 */
public interface BufferFactory {
  ByteBuffer newByteBuffer(int length);

  default ShortBuffer newShortBuffer(int length) {
    return newByteBuffer(2 * length).asShortBuffer();
  }

  default IntBuffer newIntBuffer(int length) {
    return newByteBuffer(4 * length).asIntBuffer();
  }

  default LongBuffer newLongBuffer(int length) {
    return newByteBuffer(8 * length).asLongBuffer();
  }

  default FloatBuffer newFloatBuffer(int length) {
    return newByteBuffer(4 * length).asFloatBuffer();
  }

  default DoubleBuffer newDoubleBuffer(int length) {
    return newByteBuffer(8 * length).asDoubleBuffer();
  }
}
