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
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Objects;

/**
 * FloatBufferData
 * ===============
 *
 * Concrete FloatData implementation that stores primitive values in a {@link FloatBuffer}. Because
 * Java NIO buffers use integer indices, the maximum length of this type of DataBuffer is restricted
 * by the integer max value even though the interface supports `long`.
 *
 * The buffer returned by {@link #getSource()} is a new instance formed by {@link
 * FloatBuffer#duplicate()} so that modifications to element values are mirrored but changes to the
 * returned instance's limit and position do not effect this DataBuffer's state.
 *
 * @author Michael Ludwig
 */
public class FloatBufferData extends FloatData implements DataView<FloatBuffer> {
  private final FloatBuffer buffer;

  /**
   * Creates a new FloatBufferData that allocates a FloatBuffer of `length` with the currently
   * configured buffer factory.
   *
   * @param length
   *     The length of the data buffer
   * @see Data#getBufferFactory()
   */
  public FloatBufferData(int length) {
    this(Data.getBufferFactory().newFloatBuffer(length));
  }

  /**
   * Creates a new FloatBufferData that wraps the given FloatBuffer. The data buffer creates a safe
   * duplicate using {@link FloatBuffer#duplicate()} and uses the entire capacity of the buffer,
   * ignoring the state of position or limit at the time of this constructor call.
   *
   * @param buffer
   *     The buffer to wrap
   * @throws NullPointerException
   *     if `buffer` is null
   */
  public FloatBufferData(FloatBuffer buffer) {
    Arguments.notNull("buffer", buffer);
    this.buffer = buffer.duplicate();
    // Preserve the 0 -> capacity() rule for stored buffer without modifying original buffer's
    // position and limit
    this.buffer.clear();
  }

  @Override
  public float get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public long getLength() {
    return buffer.capacity();
  }

  @Override
  public FloatBuffer getSource() {
    return buffer.duplicate();
  }

  @Override
  public void getValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize with bulk get in FloatBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("FloatBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.get(values, offset, length);
    buffer.clear();
  }

  @Override
  public void getValues(long dataIndex, FloatBuffer values) {
    // Optimize with FloatBuffer put
    Arguments.checkArrayRange("FloatBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    values.put(buffer);
    buffer.clear();
  }

  @Override
  public boolean isBigEndian() {
    return Objects.equals(buffer.order(), ByteOrder.BIG_ENDIAN);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && Objects.equals(buffer.order(), ByteOrder.nativeOrder());
  }

  @Override
  public void set(long index, float value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public void setValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize with bulk put in FloatBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("FloatBufferData", getLength(), dataIndex, length);

    setBufferRange(dataIndex, length);
    buffer.put(values, offset, length);
    buffer.clear();
  }

  @Override
  public void setValues(long dataIndex, FloatBuffer values) {
    // Optimize with FloatBuffer put
    Arguments.checkArrayRange("FloatBufferData", getLength(), dataIndex, values.remaining());

    setBufferRange(dataIndex, values.remaining());
    buffer.put(values);
    buffer.clear();
  }

  private void setBufferRange(long dataIndex, int length) {
    int bufferOffset = Math.toIntExact(dataIndex);
    buffer.limit(bufferOffset + length).position(bufferOffset);
  }
}
