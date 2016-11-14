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
package com.lhkbob.imaje.data.array;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ShortBuffer;

/**
 * ShortArrayData
 * ==============
 *
 * Concrete ShortData implementation that stores primitive values in a `short[]`. Because Java
 * arrays use integer indices, the maximum length of this type of DataBuffer is restricted by the
 * integer max value even though the interface supports `long`.
 *
 * @author Michael Ludwig
 */
public class ShortArrayData extends ShortData implements DataView<short[]> {
  private final short[] array;

  /**
   * Create a new ShortArrayData that allocates a new array of `length`.
   *
   * @param length
   *     The length of the data buffer
   */
  public ShortArrayData(int length) {
    this(new short[length]);
  }

  /**
   * Create a new ShortArrayData that wraps the provided `array`. Modifications to this DataBuffer
   * are reflected in `array`'s state and modifications directly to the array are reflected by
   * the buffer.
   *
   * @param array
   *     The array to wrap
   * @throws NullPointerException
   *     if `array` is null
   */
  public ShortArrayData(short[] array) {
    Arguments.notNull("array", array);
    this.array = array;
  }

  @Override
  public short get(long index) {
    return array[Math.toIntExact(index)];
  }

  @Override
  public short[] getSource() {
    return array;
  }

  @Override
  public long getLength() {
    return array.length;
  }

  @Override
  public boolean isBigEndian() {
    return true;
  }

  @Override
  public boolean isGPUAccessible() {
    // Arrays are not guaranteed contiguous so a pointer isn't available to transfer to the GPU
    return false;
  }

  @Override
  public void set(long index, short value) {
    array[Math.toIntExact(index)] = value;
  }

  @Override
  public void set(long dataIndex, short[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ShortArrayData", getLength(), dataIndex, length);

    System.arraycopy(values, offset, array, Math.toIntExact(dataIndex), length);
  }

  @Override
  public void set(long dataIndex, ShortBuffer values) {
    // Optimize with bulk get defined in ShortBuffer
    Arguments.checkArrayRange("ShortArrayData", getLength(), dataIndex, values.remaining());
    values.get(array, Math.toIntExact(dataIndex), values.remaining());
  }

  @Override
  public void get(long dataIndex, short[] values, int offset, int length) {
    // Optimize with System.arraycopy
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ShortArrayData", getLength(), dataIndex, length);

    System.arraycopy(array, Math.toIntExact(dataIndex), values, offset, length);
  }

  @Override
  public void get(long dataIndex, ShortBuffer values) {
    // Optimize with bulk put defined in ShortBuffer
    Arguments.checkArrayRange("ShortArrayData", getLength(), dataIndex, values.remaining());
    values.put(array, Math.toIntExact(dataIndex), values.remaining());
  }
}
