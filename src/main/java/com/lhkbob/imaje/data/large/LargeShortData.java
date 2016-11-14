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
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ShortBuffer;

/**
 * LargeShortData
 * ==============
 *
 * ShortData implementation that concatenates multiple ShortData instances into a single data buffer
 * that can have more elements than representable with an `int`.
 *
 * @author Michael Ludwig
 */
public class LargeShortData extends ShortData {
  private final BufferConcatentation<ShortData> data;

  /**
   * Create a new LargeShortBuffer that wraps the given ShortData sources. See {@link
   * BufferConcatentation#BufferConcatentation(DataBuffer[])} for requirements of the source
   * buffers.
   *
   * @param sources
   *     The source buffers to concatenate and wrap
   */
  public LargeShortData(ShortData[] sources) {
    data = new BufferConcatentation<>(sources);
  }

  @Override
  public short get(long index) {
    return data.getSource(index).get(data.getIndexInSource(index));
  }

  @Override
  public void get(long dataIndex, short[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeShortData", getLength(), dataIndex, length);

    data.bulkOperation(ShortData::get, dataIndex, values, offset, length);
  }

  @Override
  public void get(long dataIndex, ShortBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeShortData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    data.bulkOperation(
        this::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
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
  public void set(long index, short value) {
    data.getSource(index).set(data.getIndexInSource(index), value);
  }

  @Override
  public void set(long dataIndex, ShortBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeShortData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    data.bulkOperation(
        this::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void set(long dataIndex, short[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeShortData", getLength(), dataIndex, length);

    data.bulkOperation(ShortData::set, dataIndex, values, offset, length);
  }

  private void getSubSource(
      ShortData source, long srcOffset, ShortBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.get(srcOffset, get);
  }

  private void setSubSource(
      ShortData source, long srcOffset, ShortBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.set(srcOffset, values);
  }
}
