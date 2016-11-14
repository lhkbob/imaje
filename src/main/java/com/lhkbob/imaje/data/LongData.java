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

import com.lhkbob.imaje.data.array.LongArrayData;
import com.lhkbob.imaje.data.large.LargeLongData;
import com.lhkbob.imaje.data.nio.LongBufferData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.LongBuffer;

/**
 * LongData
 * ========
 *
 * BitData implementation that stores 64-bit fields as `long` values. It adds individual element
 * setters and getters that operate on `long` values. It adds bulk data read and write operations
 * that operate on `long[]` and {@link LongBuffer}.
 *
 * @author Michael Ludwig
 */
public abstract class LongData implements BitData {
  /**
   * LongData.Numeric
   * ================
   *
   * A NumericData implementation that wraps a LongData instance and interprets its data as 2's
   * complement signed integers. The integer values are lifted to `double` the same way longs are
   * normally widened to doubles in Java. Double values stored are clamped to the range of an 64-bit
   * signed integer (i.e. Long.MIN_VALUE to Long.MAX_VALUE) and are rounded to the nearest integer.
   *
   * @author Michael Ludwig
   */
  public static class Numeric extends NumericData<LongData> implements DataView<LongData> {
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

  /**
   * @param index
   *     The index to lookup
   * @return Get the long value at `index`.
   */
  public abstract long get(long index);

  /**
   * Get the values of this LongData and store them into the given array `values`.
   * Values are read starting at `dataIndex` from this buffer and will fill the entire array.
   * This is equivalent to `get(dataIndex, values, 0, values.length)`.
   *
   * @param dataIndex
   *     The data index into this buffer that values are read from
   * @param values
   *     The destination array that gets the values of this buffer
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this`, or if `length` would
   *     access bad elements of `values`.
   */
  public void get(long dataIndex, long[] values) {
    get(dataIndex, values, 0, values.length);
  }

  /**
   * Get the values of this LongData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into `values` starting at `offset`. `length` longs are
   * read into `values`.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The destination array receiving the longs from this buffer
   * @param offset
   *     The index into the destination array to start writing the longs
   * @param length
   *     The number of longs to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void get(long dataIndex, long[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = get(dataIndex + i);
    }
  }

  /**
   * Get the values of this LongData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into the LongBuffer starting at the LongBuffer's position.
   * Longs are read into the buffer up to its configured limit. After invoking this method, the
   * buffer's position will be at its limit.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The LongBuffer destination that receives longs from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of longs to read (based on the remaining longs in `values`)
   *     would access bad elements
   */
  public void get(long dataIndex, LongBuffer values) {
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, get(dataIndex + i));
    }
    values.position(values.limit());
  }

  @Override
  public final int getBitSize() {
    return Long.SIZE;
  }

  @Override
  public final long getBits(long index) {
    return get(index);
  }

  @Override
  public void set(long writeIndex, DataBuffer data, long readIndex, long length) {
    if (data instanceof LongArrayData) {
      // Extract the long[] and rely on array-based set() implementation
      set(writeIndex, ((LongArrayData) data).getSource(), Math.toIntExact(readIndex),
          Math.toIntExact(length));
    } else if (data instanceof LongBufferData) {
      // Extract the LongBuffer and rely on the buffer-based set() implementation
      LongBuffer source = ((LongBufferData) data).getSource();
      source.limit(Math.toIntExact(readIndex + length)).position(Math.toIntExact(readIndex));
      set(writeIndex, source);
    } else if (data instanceof LargeLongData) {
      // Break apart the data source into its component values
      LargeLongData large = (LargeLongData) data;
      large.get(readIndex, this, writeIndex, length);
    } else if (data instanceof LongData) {
      // General implementation that supports (naively) all other possible LongData implementations
      LongData bd = (LongData) data;
      for (long i = 0; i < length; i++) {
        set(writeIndex + i, bd.get(readIndex + i));
      }
    } else {
      throw new UnsupportedOperationException(
          "Cannot copy values from unsupported buffer: " + data);
    }
  }

  /**
   * Set the long at `index` to `value`.
   *
   * @param index
   *     The index to modify
   * @param value
   *     The new value
   */
  public abstract void set(long index, long value);

  /**
   * Set the values of this LongData to those in the given array `values`. Values are written
   * starting at `dataIndex` into this buffer and will write the entire array. This is equivalent to
   * `set(dataIndex, values, 0, values.length)`.
   *
   * @param dataIndex
   *     The data index into this buffer that values are written to
   * @param values
   *     The source array that provides the new values of this buffer
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this`, or if `length` would
   *     access bad elements of `values`.
   */
  public void set(long dataIndex, long[] values) {
    set(dataIndex, values, 0, values.length);
  }

  /**
   * Set the values of this LongData to those in `values`. Values are written into this buffer
   * starting at `dataIndex` and read from `values` starting at `offset`. `length` longs are
   * taken from `values`.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The source array providing the longs to this buffer
   * @param offset
   *     The index into the source array to start reading the longs
   * @param length
   *     The number of longs to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void set(long dataIndex, long[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values[offset + i]);
    }
  }

  /**
   * Set the values of this LongData to those into `values`. Data is written to this buffer starting
   * at `dataIndex` and read from the LongBuffer starting at the LongBuffer's position. Longs are
   * written from the buffer up to its configured limit. After invoking this method, the buffer's
   * position will be at its limit.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The LongBuffer source that provides longs from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of longs to read (based on the remaining longs in `values`)
   *     would access bad elements
   */
  public void set(long dataIndex, LongBuffer values) {
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      set(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  @Override
  public final void setBits(long index, long value) {
    set(index, value);
  }
}
