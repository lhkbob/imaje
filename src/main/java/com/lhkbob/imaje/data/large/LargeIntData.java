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
package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.IntBuffer;

/**
 * LargeIntData
 * ============
 *
 * IntData implementation that concatenates multiple IntData instances into a single data buffer
 * that can have more elements than representable with an `int`.
 *
 * @author Michael Ludwig
 */
public class LargeIntData extends IntData {
  private final BufferConcatentation<IntData> data;

  /**
   * Create a new LargeIntBuffer that wraps the given IntData sources. See {@link
   * BufferConcatentation#BufferConcatentation(DataBuffer[])} for requirements of the source
   * buffers.
   *
   * @param sources
   *     The source buffers to concatenate and wrap
   */
  public LargeIntData(IntData[] sources) {
    this.data = new BufferConcatentation<>(sources);
  }

  @Override
  public int get(long index) {
    return data.getSource(index).get(data.getIndexInSource(index));
  }

  @Override
  public void get(long dataIndex, int[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, length);

    data.bulkOperation(IntData::get, dataIndex, values, offset, length);
  }

  @Override
  public void get(long dataIndex, IntBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    data.bulkOperation(
        LargeIntData::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  /**
   * Get the values of this large int data and store them into `dst`. Values are read from this
   * data starting at `getIndex`, and stored starting at `dstIndex` in `dst`. `length` values are
   * copied. This works efficiently by invoking {@link DataBuffer#set(long, DataBuffer, long, long)}
   * multiple times for the buffer sources of this large data set that intersect with the range to
   * copy.
   *
   * @param getIndex
   *     The index into this data buffer to start copying from
   * @param dst
   *     The data buffer that receives the int values from this source
   * @param dstIndex
   *     The index in `dst` for the first copied int
   * @param length
   *     The number of values to copy
   * @throws IndexOutOfBoundsException
   *     if bad indices would be accessed based on index and length
   */
  public void get(long getIndex, IntData dst, long dstIndex, long length) {
    data.copyToDataBuffer(getIndex, dst, dstIndex, length);
  }

  @Override
  public long getLength() {
    return data.getLength();
  }

  @Override
  public boolean isBigEndian() {
    return data.isBigEndian();
  }

  @Override
  public boolean isGPUAccessible() {
    // Although the GPU might be able to support data sets that have more than a 32 bit index,
    // because Java can't allocate a contiguous array that long there is no way to have such a long
    // data source represented by a single pointer; thus this form of large data source cannot be
    // GPU accessible
    return false;
  }

  @Override
  public void set(long index, int value) {
    data.getSource(index).set(data.getIndexInSource(index), value);
  }

  @Override
  public void set(long dataIndex, IntBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    data.bulkOperation(
        LargeIntData::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void set(long dataIndex, int[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, length);

    data.bulkOperation(IntData::set, dataIndex, values, offset, length);
  }

  private static void getSubSource(
      IntData source, long srcOffset, IntBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.get(srcOffset, get);
  }

  private static void setSubSource(
      IntData source, long srcOffset, IntBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.set(srcOffset, values);
  }
}
