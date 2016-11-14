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

import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * DirectBufferFactory
 * ===================
 *
 * BufferFactory implementation that creates NIO buffers using {@link
 * ByteBuffer#allocateDirect(int)}. It can be configured to use a particular byte order as well.
 *
 * @author Michael Ludwig
 */
public class DirectBufferFactory implements BufferFactory {
  private final ByteOrder byteOrder;

  /**
   * Create a factory that uses big endian for its byte order.
   */
  public DirectBufferFactory() {
    this(ByteOrder.BIG_ENDIAN);
  }

  /**
   * Create a factory that creates direct buffers with the give `byteOrder`.
   *
   * @param byteOrder
   *     The byte order of all created buffers
   * @throws NullPointerException
   *     if `byteOrder` is null
   */
  public DirectBufferFactory(ByteOrder byteOrder) {
    Arguments.notNull("byteOrder", byteOrder);
    this.byteOrder = byteOrder;
  }

  /**
   * @return A new DirectBufferFactory that uses the native byte order of the current system.
   */
  public static DirectBufferFactory nativeFactory() {
    return new DirectBufferFactory(ByteOrder.nativeOrder());
  }

  @Override
  public ByteBuffer newByteBuffer(int length) {
    return ByteBuffer.allocateDirect(length).order(byteOrder);
  }
}
