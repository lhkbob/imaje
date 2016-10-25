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
package com.lhkbob.imaje.data;

import com.lhkbob.imaje.util.Arguments;

import java.nio.LongBuffer;

/**
 *
 */
public interface LongData extends BitData {
  class Numeric implements NumericData<LongData>, DataView<LongData> {
    private final LongData source;

    public Numeric(LongData source) {
      Arguments.notNull("source", source);
      this.source = source;
    }

    @Override
    public LongData asBitData() {
      return source;
    }

    @Override
    public int getBitSize() {
      return source.getBitSize();
    }

    @Override
    public long getLength() {
      return source.getLength();
    }

    @Override
    public LongData getSource() {
      return source;
    }

    @Override
    public double getValue(long index) {
      return source.get(index);
    }

    @Override
    public boolean isBigEndian() {
      return source.isBigEndian();
    }

    @Override
    public boolean isGPUAccessible() {
      return source.isGPUAccessible();
    }

    @Override
    public void setValue(long index, double value) {
      // Clamp to long boundary values so casting roll-over from double isn't surprising
      value = Math.max(Long.MIN_VALUE, Math.min(value, Long.MAX_VALUE));
      source.set(index, Math.round(value));
    }
  }

  long get(long index);

  default void get(long dataIndex, long[] values) {
    get(dataIndex, values, 0, values.length);
  }

  default void get(long dataIndex, long[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = get(dataIndex + i);
    }
  }

  default void get(long dataIndex, LongBuffer values) {
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, get(dataIndex + i));
    }
    values.position(values.limit());
  }

  @Override
  default int getBitSize() {
    return Long.SIZE;
  }

  @Override
  default long getBits(long index) {
    return get(index);
  }

  void set(long index, long value);

  default void set(long dataIndex, long[] values) {
    set(dataIndex, values, 0, values.length);
  }

  default void set(long dataIndex, long[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values[offset + i]);
    }
  }

  default void set(long dataIndex, LongBuffer values) {
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      set(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  @Override
  default void setBits(long index, long value) {
    set(index, value);
  }
}
