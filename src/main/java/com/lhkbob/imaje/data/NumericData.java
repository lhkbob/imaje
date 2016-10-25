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

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 *
 */
public interface NumericData<T extends BitData> extends DataBuffer {
  double getValue(long index);

  void setValue(long index, double value);

  T asBitData();

  default void setValues(long dataIndex, float[] values) {
    setValues(dataIndex, values, 0, values.length);
  }

  default void setValues(long dataIndex, double[] values) {
    setValues(dataIndex, values, 0, values.length);
  }

  default void setValues(long dataIndex, float[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      setValue(dataIndex + i, values[offset + i]);
    }
  }

  default void setValues(long dataIndex, double[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      setValue(dataIndex + i, values[offset + i]);
    }
  }

  default void setValues(long dataIndex, FloatBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      setValue(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  default void setValues(long dataIndex, DoubleBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      setValue(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  default void getValues(long dataIndex, float[] values) {
    getValues(dataIndex, values, 0, values.length);
  }

  default void getValues(long dataIndex, double[] values) {
    getValues(dataIndex, values, 0, values.length);
  }

  default void getValues(long dataIndex, float[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = (float) getValue(dataIndex + i);
    }
  }

  default void getValues(long dataIndex, double[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = getValue(dataIndex + i);
    }
  }

  default void getValues(long dataIndex, FloatBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, (float) getValue(dataIndex + i));
    }
    values.position(values.limit());
  }

  default void getValues(long dataIndex, DoubleBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, getValue(dataIndex + i));
    }
    values.position(values.limit());
  }
}
