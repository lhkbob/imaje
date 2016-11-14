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
package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.Data;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * BufferFactory
 * =============
 *
 * BufferFactory is a factory interface for creating NIO buffers. The performance characteristics of
 * NIO buffers are heavily dependent on minimizing the class variants loaded into the JVM.
 * Essentially, mixing array-back buffers and direct buffers hurts much of the JIT optimizations
 * available to the JVM. Thus, to help avoid this scenario, all NIO buffer allocation by library
 * code (and ideally application code) should go through the BufferFactory configured in {@link
 * Data}.
 *
 * Implementations are provided for array-based NIO buffers and direct buffers.
 * Additional implementations can be created and set as the active buffer factory as needed.
 *
 * @author Michael Ludwig
 * @see Data#getBufferFactory()
 */
public interface BufferFactory {
  /**
   * Create a new ByteBuffer of `length` bytes.
   *
   * @param length
   *     The capacity of the new buffer
   * @return The new byte buffer
   */
  ByteBuffer newByteBuffer(int length);

  /**
   * Create a new ShortBuffer of `length` shorts. By default calls `newByteBuffer(2 * length)` and
   * then relies on that instances {@link ByteBuffer#asShortBuffer()}.
   *
   * @param length
   *     The capacity of the new buffer
   * @return The new short buffer
   */
  default ShortBuffer newShortBuffer(int length) {
    return newByteBuffer(2 * length).asShortBuffer();
  }

  /**
   * Create a new IntBuffer of `length` shorts. By default calls `newByteBuffer(4 * length)` and
   * then relies on that instances {@link ByteBuffer#asIntBuffer()}.
   *
   * @param length
   *     The capacity of the new buffer
   * @return The new int buffer
   */
  default IntBuffer newIntBuffer(int length) {
    return newByteBuffer(4 * length).asIntBuffer();
  }

  /**
   * Create a new LongBuffer of `length` shorts. By default calls `newByteBuffer(8 * length)` and
   * then relies on that instances {@link ByteBuffer#asLongBuffer()}.
   *
   * @param length
   *     The capacity of the new buffer
   * @return The new long buffer
   */
  default LongBuffer newLongBuffer(int length) {
    return newByteBuffer(8 * length).asLongBuffer();
  }

  /**
   * Create a new FloatBuffer of `length` shorts. By default calls `newByteBuffer(4 * length)` and
   * then relies on that instances {@link ByteBuffer#asFloatBuffer()}.
   *
   * @param length
   *     The capacity of the new buffer
   * @return The new short buffer
   */
  default FloatBuffer newFloatBuffer(int length) {
    return newByteBuffer(4 * length).asFloatBuffer();
  }

  /**
   * Create a new DoubleBuffer of `length` shorts. By default calls `newByteBuffer(8 * length)` and
   * then relies on that instances {@link ByteBuffer#asDoubleBuffer()}.
   *
   * @param length
   *     The capacity of the new buffer
   * @return The new short buffer
   */
  default DoubleBuffer newDoubleBuffer(int length) {
    return newByteBuffer(8 * length).asDoubleBuffer();
  }
}
