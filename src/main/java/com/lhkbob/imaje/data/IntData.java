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

import com.lhkbob.imaje.data.array.IntArrayData;
import com.lhkbob.imaje.data.large.LargeIntData;
import com.lhkbob.imaje.data.nio.IntBufferData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.IntBuffer;

/**
 * IntData
 * =======
 *
 * BitData implementation that stores 32-bit fields as `int` values. It adds individual element
 * setters and getters that operate on `int` values. It adds bulk data read and write operations
 * that operate on `int[]` and {@link IntBuffer}.
 *
 * @author Michael Ludwig
 */
public abstract class IntData implements BitData {
  /**
   * IntData.Numeric
   * ===============
   *
   * A NumericData implementation that wraps an IntData instance and interprets its data as 2's
   * complement signed integers. The integer values are lifted to `double` the same way bytes are
   * normally widened to doubles in Java. Double values stored are clamped to the range of an 32-bit
   * signed integer (i.e. Integer.MIN_VALUE to Integer.MAX_VALUE) and are rounded to the nearest
   * integer.
   *
   * @author Michael Ludwig
   */
  public static class Numeric extends NumericData<IntData> implements DataView<IntData> {
    private final IntData source;

    public Numeric(IntData source) {
      Arguments.notNull("source", source);
      this.source = source;
    }

    @Override
    public IntData asBitData() {
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
    public IntData getSource() {
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
      // Clamp to int boundary values so casting roll-over from double isn't surprising
      value = Math.max(Integer.MIN_VALUE, Math.min(value, Integer.MAX_VALUE));
      source.set(index, (int) Math.round(value));
    }
  }

  /**
   * @param index
   *     The index to lookup
   * @return Get the int value at `index`.
   */
  public abstract int get(long index);

  /**
   * Get the values of this IntData and store them into the given array `values`. Values are read
   * starting at `dataIndex` from this buffer and will fill the entire array. This is equivalent to
   * `get(dataIndex, values, 0, values.length)`.
   *
   * @param dataIndex
   *     The data index into this buffer that values are read from
   * @param values
   *     The destination array that gets the values of this buffer
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this`, or if `length` would
   *     access bad elements of `values`.
   */
  public void get(long dataIndex, int[] values) {
    get(dataIndex, values, 0, values.length);
  }

  /**
   * Get the values of this IntData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into `values` starting at `offset`. `length` bytes are
   * read into `values`.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The destination array receiving the int from this buffer
   * @param offset
   *     The index into the destination array to start writing the ints
   * @param length
   *     The number of bytes to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void get(long dataIndex, int[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("IntData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = get(dataIndex + i);
    }
  }

  /**
   * Get the values of this IntData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into the IntBuffer starting at the IntBuffer's position.
   * Ints are read into the buffer up to its configured limit. After invoking this method, the
   * buffer's position will be at its limit.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The IntBuffer destination that receives ints from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of ints to read (based on the remaining ints in `values`)
   *     would access bad elements
   */
  public void get(long dataIndex, IntBuffer values) {
    Arguments.checkArrayRange("IntData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, get(dataIndex + i));
    }
    values.position(values.limit());
  }

  @Override
  public final int getBitSize() {
    return Integer.SIZE;
  }

  @Override
  public final long getBits(long index) {
    return get(index);
  }

  @Override
  public void set(long writeIndex, DataBuffer data, long readIndex, long length) {
    if (data instanceof IntArrayData) {
      // Extract the int[] and rely on array-based set() implementation
      set(writeIndex, ((IntArrayData) data).getSource(), Math.toIntExact(readIndex),
          Math.toIntExact(length));
    } else if (data instanceof IntBufferData) {
      // Extract the IntBuffer and rely on the buffer-based set() implementation
      IntBuffer source = ((IntBufferData) data).getSource();
      source.limit(Math.toIntExact(readIndex + length)).position(Math.toIntExact(readIndex));
      set(writeIndex, source);
    } else if (data instanceof LargeIntData) {
      // Break apart the data source into its component values
      LargeIntData large = (LargeIntData) data;
      large.get(readIndex, this, writeIndex, length);
    } else if (data instanceof IntData) {
      // General implementation that supports (naively) all other possible IntData implementations
      IntData bd = (IntData) data;
      for (long i = 0; i < length; i++) {
        set(writeIndex + i, bd.get(readIndex + i));
      }
    } else {
      throw new UnsupportedOperationException(
          "Cannot copy values from unsupported buffer: " + data);
    }
  }

  /**
   * Set the int at `index` to `value`.
   *
   * @param index
   *     The index to modify
   * @param value
   *     The new value
   */
  public abstract void set(long index, int value);

  /**
   * Set the values of this IntData to those in the given array `values`. Values are written
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
  public void set(long dataIndex, int[] values) {
    set(dataIndex, values, 0, values.length);
  }

  /**
   * Set the values of this IntData to those in `values`. Values are written into this buffer
   * starting at `dataIndex` and read from `values` starting at `offset`. `length` bytes are
   * taken from `values`.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The source array providing the ints to this buffer
   * @param offset
   *     The index into the source array to start reading the ints
   * @param length
   *     The number of ints to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void set(long dataIndex, int[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("IntData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values[offset + i]);
    }
  }

  /**
   * Set the values of this IntData to those into `values`. Data is written to this buffer starting
   * at `dataIndex` and read from the IntBuffer starting at the IntBuffer's position. Ints are
   * written from the buffer up to its configured limit. After invoking this method, the buffer's
   * position will be at its limit.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The IntBuffer source that provides bytes from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of ints to read (based on the remaining ints in `values`)
   *     would access bad elements
   */
  public void set(long dataIndex, IntBuffer values) {
    Arguments.checkArrayRange("IntData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      set(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  @Override
  public final void setBits(long index, long value) {
    set(index, (int) value);
  }
}
