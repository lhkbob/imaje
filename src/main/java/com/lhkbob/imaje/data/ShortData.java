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

import com.lhkbob.imaje.data.array.ShortArrayData;
import com.lhkbob.imaje.data.nio.ShortBufferData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ShortBuffer;

/**
 * ShortData
 * ========
 *
 * BitData implementation that stores 16-bit fields as `short` values. It adds individual element
 * setters and getters that operate on `short` values. It adds bulk data read and write operations
 * that operate on `short[]` and {@link ShortBuffer}.
 *
 * @author Michael Ludwig
 */
public abstract class ShortData implements BitData {
  /**
   * ShortData.Numeric
   * ================
   *
   * A NumericData implementation that wraps a ShortData instance and interprets its data as 2's
   * complement signed integers. The integer values are lifted to `double` the same way shorts are
   * normally widened to doubles in Java. Double values stored are clamped to the range of an 16-bit
   * signed integer (i.e. Short.MIN_VALUE to Short.MAX_VALUE) and are rounded to the nearest
   * integer.
   *
   * @author Michael Ludwig
   */
  public static class Numeric extends NumericData<ShortData> implements DataView<ShortData> {
    private final ShortData source;

    public Numeric(ShortData source) {
      Arguments.notNull("source", source);
      this.source = source;
    }

    @Override
    public ShortData asBitData() {
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
    public ShortData getSource() {
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
      // Clamp to short boundary values so casting roll-over from double isn't surprising
      value = Math.max(Short.MIN_VALUE, Math.min(value, Short.MAX_VALUE));
      source.set(index, (short) Math.round(value));
    }
  }

  /**
   * @param index
   *     The index to lookup
   * @return Get the long value at `index`.
   */
  public abstract short get(long index);

  /**
   * Get the values of this ShortData and store them into the given array `values`.
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
  public void get(long dataIndex, short[] values) {
    get(dataIndex, values, 0, values.length);
  }

  /**
   * Get the values of this ShortData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into `values` starting at `offset`. `length` shorts are
   * read into `values`.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The destination array receiving the shorts from this buffer
   * @param offset
   *     The index into the destination array to start writing the shorts
   * @param length
   *     The number of shorts to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void get(long dataIndex, short[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("ShortData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = get(dataIndex + i);
    }
  }

  /**
   * Get the values of this ShortData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into the ShortBuffer starting at the ShortBuffer's position.
   * Shorts are read into the buffer up to its configured limit. After invoking this method, the
   * buffer's position will be at its limit.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The ShortBuffer destination that receives shorts from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of shorts to read (based on the remaining shorts in `values`)
   *     would access bad elements
   */
  public void get(long dataIndex, ShortBuffer values) {
    Arguments.checkArrayRange("ShortData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, get(dataIndex + i));
    }
    values.position(values.limit());
  }

  @Override
  public final int getBitSize() {
    return Short.SIZE;
  }

  @Override
  public final long getBits(long index) {
    return get(index);
  }

  @Override
  public void set(long writeIndex, DataBuffer data, long readIndex, long length) {
    if (data instanceof ShortArrayData) {
      set(writeIndex, ((ShortArrayData) data).getSource(), Math.toIntExact(readIndex),
          Math.toIntExact(length));
    } else if (data instanceof ShortBufferData) {
      ShortBuffer source = ((ShortBufferData) data).getSource();
      source.limit(Math.toIntExact(readIndex + length)).position(Math.toIntExact(readIndex));
      set(writeIndex, source);
    } else if (data instanceof ShortData) {
      ShortData bd = (ShortData) data;
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
  public abstract void set(long index, short value);

  /**
   * Set the values of this ShortData to those in the given array `values`. Values are written
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
  public void set(long dataIndex, short[] values) {
    set(dataIndex, values, 0, values.length);
  }

  /**
   * Set the values of this ShortData to those in `values`. Values are written into this buffer
   * starting at `dataIndex` and read from `values` starting at `offset`. `length` shorts are
   * taken from `values`.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The source array providing the shorts to this buffer
   * @param offset
   *     The index into the source array to start reading the shorts
   * @param length
   *     The number of shorts to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void set(long dataIndex, short[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("ShortData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values[offset + i]);
    }
  }

  /**
   * Set the values of this ShortData to those into `values`. Data is written to this buffer
   * starting at `dataIndex` and read from the ShortBuffer starting at the ShortBuffer's position.
   * Shorts are written from the buffer up to its configured limit. After invoking this method, the
   * buffer's position will be at its limit.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The ShortBuffer source that provides shorts from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of shorts to read (based on the remaining shorts in `values`)
   *     would access bad elements
   */
  public void set(long dataIndex, ShortBuffer values) {
    Arguments.checkArrayRange("ShortData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      set(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  @Override
  public final void setBits(long index, long value) {
    set(index, (short) value);
  }
}
