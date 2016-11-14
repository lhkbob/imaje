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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * ArrayBufferFactory
 * ==================
 *
 * BufferFactory implementation that creates NIO buffers using {@link
 * ByteBuffer#allocate(int)} and the `allocate()` methods of the other buffer classes.
 * The byte order is always big endian.
 *
 * @author Michael Ludwig
 */
public class ArrayBufferFactory implements BufferFactory {
  @Override
  public ByteBuffer newByteBuffer(int length) {
    return ByteBuffer.allocate(length);
  }

  @Override
  public DoubleBuffer newDoubleBuffer(int length) {
    return DoubleBuffer.allocate(length);
  }

  @Override
  public FloatBuffer newFloatBuffer(int length) {
    return FloatBuffer.allocate(length);
  }

  @Override
  public IntBuffer newIntBuffer(int length) {
    return IntBuffer.allocate(length);
  }

  @Override
  public LongBuffer newLongBuffer(int length) {
    return LongBuffer.allocate(length);
  }

  @Override
  public ShortBuffer newShortBuffer(int length) {
    return ShortBuffer.allocate(length);
  }
}
