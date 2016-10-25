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

import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.IntBuffer;

/**
 *
 */
public class LargeIntData extends AbstractLargeDataSource<IntData> implements IntData {
  public LargeIntData(IntData[] sources) {
    super(sources);
  }

  @Override
  public int get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void get(long dataIndex, int[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, length);

    bulkOperation(IntData::get, dataIndex, values, offset, length);
  }

  @Override
  public void get(long dataIndex, IntBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);  }

  @Override
  public void set(long index, int value) {
    getSource(index).set(getIndexInSource(index), value);
  }

  @Override
  public void set(long dataIndex, IntBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void set(long dataIndex, int[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, length);

    bulkOperation(IntData::set, dataIndex, values, offset, length);
  }

  private void getSubSource(
      IntData source, long srcOffset, IntBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.get(srcOffset, get);
  }

  private void setSubSource(
      IntData source, long srcOffset, IntBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.set(srcOffset, values);
  }
}
