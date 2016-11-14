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
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * ShortBufferData
 * ===============
 *
 * Concrete ShortData implementation that stores primitive values in a {@link ShortBuffer}. Because
 * Java NIO buffers use integer indices, the maximum length of this type of DataBuffer is restricted
 * by the integer max value even though the interface supports `long`.
 *
 * The buffer returned by {@link #getSource()} is a new instance formed by {@link
 * ShortBuffer#duplicate()} so that modifications to element values are mirrored but changes to the
 * returned instance's limit and position do not effect this DataBuffer's state.
 *
 * @author Michael Ludwig
 */
public class ShortBufferData extends ShortData implements DataView<ShortBuffer> {
  private final ShortBuffer buffer;

  /**
   * Creates a new ShortBufferData that allocates a ShortBuffer of `length` with the currently
   * configured buffer factory.
   *
   * @param length
   *     The length of the data buffer
   * @see Data#getBufferFactory()
   */
  public ShortBufferData(int length) {
    this(Data.getBufferFactory().newShortBuffer(length));
  }

  /**
   * Creates a new ShortBufferData that wraps the given ShortBuffer. The data buffer creates a safe
   * duplicate using {@link ShortBuffer#duplicate()} and uses the entire capacity of the buffer,
   * ignoring the state of position or limit at the time of this constructor call.
   *
   * @param buffer
   *     The buffer to wrap
   * @throws NullPointerException
   *     if `buffer` is null
   */
  public ShortBufferData(ShortBuffer buffer) {
    Arguments.notNull("buffer", buffer);
    this.buffer = buffer.duplicate();
    // Preserve the 0 -> capacity() rule for stored buffer without modifying original buffer's
    // position and limit
    this.buffer.clear();
  }

  @Override
  public short get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public ShortBuffer getSource() {
    return buffer.duplicate();
  }

  @Override
  public long getLength() {
    return buffer.capacity();
  }

  @Override
  public boolean isBigEndian() {
    return buffer.order().equals(ByteOrder.BIG_ENDIAN);
  }

  @Override
  public void set(long index, short value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }

  @Override
  public void set(long dataIndex, short[] values, int offset, int length) {
    // Optimize with bulk put in ShortBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.put(values, offset, length);
    buffer.clear();
  }

  @Override
  public void set(long dataIndex, ShortBuffer values) {
    // Optimize with ShortBuffer put
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    buffer.put(values);
    buffer.clear();
  }

  @Override
  public void get(long dataIndex, short[] values, int offset, int length) {
    // Optimize with bulk get in ShortBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.get(values, offset, length);
    buffer.clear();
  }

  @Override
  public void get(long dataIndex, ShortBuffer values) {
    // Optimize with ShortBuffer put
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    values.put(buffer);
    buffer.clear();
  }

  private void setBufferRange(long dataIndex, int length) {
    int bufferOffset = Math.toIntExact(dataIndex);
    buffer.limit(bufferOffset + length).position(bufferOffset);
  }
}
