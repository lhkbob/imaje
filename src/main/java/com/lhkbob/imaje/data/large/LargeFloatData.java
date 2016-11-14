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
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 * LargeFloatData
 * ==============
 *
 * FloatData implementation that concatenates multiple FloatData instances into a single data buffer
 * that can have more elements than representable with an `int`.
 *
 * @author Michael Ludwig
 */
public class LargeFloatData extends FloatData {
  private final BufferConcatentation<FloatData> data;

  /**
   * Create a new LargeFloatBuffer that wraps the given FloatData sources. See {@link
   * BufferConcatentation#BufferConcatentation(DataBuffer[])} for requirements of the source
   * buffers.
   *
   * @param sources
   *     The source buffers to concatenate and wrap
   */
  public LargeFloatData(FloatData[] sources) {
    data = new BufferConcatentation<>(sources);
  }

  @Override
  public IntData asBitData() {
    FloatData[] sources = data.getSources();
    IntData[] bits = new IntData[sources.length];
    for (int i = 0; i < sources.length; i++) {
      bits[i] = sources[i].asBitData();
    }

    return new LargeIntData(bits);
  }

  @Override
  public float get(long index) {
    return data.getSource(index).get(data.getIndexInSource(index));
  }

  @Override
  public long getLength() {
    return data.getLength();
  }

  @Override
  public void getValues(long dataIndex, double[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeFloatData", getLength(), dataIndex, length);

    data.bulkOperation(FloatData::getValues, dataIndex, values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeFloatData", getLength(), dataIndex, length);

    data.bulkOperation(FloatData::getValues, dataIndex, values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, DoubleBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    data.bulkOperation(
        LargeFloatData::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void getValues(long dataIndex, FloatBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    data.bulkOperation(
        LargeFloatData::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
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
  public void set(long index, float value) {
    data.getSource(index).set(data.getIndexInSource(index), value);
  }

  @Override
  public void setValues(long dataIndex, double[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeFloatData", getLength(), dataIndex, length);

    data.bulkOperation(FloatData::setValues, dataIndex, values, offset, length);
  }

  @Override
  public void setValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeFloatData", getLength(), dataIndex, length);

    data.bulkOperation(FloatData::setValues, dataIndex, values, offset, length);
  }

  @Override
  public void setValues(long dataIndex, DoubleBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    data.bulkOperation(
        LargeFloatData::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void setValues(long dataIndex, FloatBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    data.bulkOperation(
        LargeFloatData::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  /**
   * Get the values of this large float data and store them into `dst`. Values are read from this
   * data starting at `getIndex`, and stored starting at `dstIndex` in `dst`. `length` values are
   * copied. This works efficiently by invoking {@link DataBuffer#set(long, DataBuffer, long, long)}
   * multiple times for the buffer sources of this large data set that intersect with the range to
   * copy.
   *
   * @param getIndex
   *     The index into this data buffer to start copying from
   * @param dst
   *     The data buffer that receives the float values from this source
   * @param dstIndex
   *     The index in `dst` for the first copied float
   * @param length
   *     The number of values to copy
   * @throws IndexOutOfBoundsException
   *     if bad indices would be accessed based on index and length
   */
  public void get(long getIndex, NumericData<?> dst, long dstIndex, long length) {
    data.copyToDataBuffer(getIndex, dst, dstIndex, length);
  }

  private static void getSubSource(
      FloatData source, long srcOffset, DoubleBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.getValues(srcOffset, get);
  }

  private static void getSubSource(
      FloatData source, long srcOffset, FloatBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.getValues(srcOffset, get);
  }

  private static void setSubSource(
      FloatData source, long srcOffset, DoubleBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.setValues(srcOffset, values);
  }

  private static void setSubSource(
      FloatData source, long srcOffset, FloatBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.setValues(srcOffset, values);
  }
}
