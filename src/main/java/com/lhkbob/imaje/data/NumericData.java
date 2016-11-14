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

import com.lhkbob.imaje.data.array.DoubleArrayData;
import com.lhkbob.imaje.data.array.FloatArrayData;
import com.lhkbob.imaje.data.large.LargeDoubleData;
import com.lhkbob.imaje.data.large.LargeFloatData;
import com.lhkbob.imaje.data.nio.DoubleBufferData;
import com.lhkbob.imaje.data.nio.FloatBufferData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 * NumericData
 * ===========
 *
 * NumericData is a DataBuffer that represents arrays of real-valued elements, i.e. they can be
 * considered and treated as a `double` regardless of the actual internal representation. Because of
 * this, all NumericData instances have more interoperability and flexibility with each other and
 * with handling float and double-typed data structures. In all cases real number line semantics are
 * preserved, loss-lessly if possible, although copying between representations with different
 * precision is allowed to discard information.
 *
 * Every NumericData can be viewed as a BitData instance with {@link #asBitData()}. The returned
 * BitData mirrors the contents of the NumericData and changes made to it reflected in the
 * NumericData.
 *
 * This class adds additional methods to get and set elements as `double` values and bulk
 * read and write operations for `float[]`, `double[]`, `FloatBuffer`, and `DoubleBuffer`.
 *
 * @author Michael Ludwig
 */
public abstract class NumericData<T extends BitData> implements DataBuffer {
  /**
   * Get the numeric value at `index`, converting from whatever implementation-dependent
   * representation into the standard Java primitive value that represents the real numbers.
   *
   * @param index
   *     The index to access
   * @return The value at `index`
   *
   * @throws IndexOutOfBoundsException
   *     if `index` accesses an invalid element
   */
  public abstract double getValue(long index);

  /**
   * Set the value at `index` stored in this buffer to `value`, converting the real number to
   * whatever internal representation is necessary to preserve the value. This conversion process
   * can be lossy but should attempt to maintain a numeric value as close to the original as
   * possible. If the underlying representation has a finite range of values, or minimum and maximum
   * values, the value should be clamped to a valid value instead of throwing an exception.
   *
   * @param index
   *     The index to modify
   * @param value
   *     The new value to store
   * @throws IndexOutOfBoundsException
   *     if `index` accesses an invalid element
   */
  public abstract void setValue(long index, double value);

  /**
   * @return A dynamic view of the numeric data exposing its underlying bit representation
   */
  public abstract T asBitData();

  @Override
  public void set(long writeIndex, DataBuffer data, long readIndex, long length) {
    if (data instanceof FloatArrayData) {
      // Extract the float[] and rely on array-based setValues() implementation
      setValues(writeIndex, ((FloatArrayData) data).getSource(), Math.toIntExact(readIndex),
          Math.toIntExact(length));
    } else if (data instanceof FloatBufferData) {
      // Extract the FloatBuffer and rely on the buffer-based setValues implementation
      FloatBuffer source = ((FloatBufferData) data).getSource();
      source.limit(Math.toIntExact(readIndex + length)).position(Math.toIntExact(readIndex));
      setValues(writeIndex, source);
    } else if (data instanceof DoubleArrayData) {
      // Extract the double[] and rely on the array-based setValues() implementation
      setValues(writeIndex, ((DoubleArrayData) data).getSource(), Math.toIntExact(readIndex),
          Math.toIntExact(length));
    } else if (data instanceof DoubleBufferData) {
      // Extract the DoubleBuffer and rely on the buffer-based setValues implementation
      DoubleBuffer source = ((DoubleBufferData) data).getSource();
      source.limit(Math.toIntExact(readIndex + length)).position(Math.toIntExact(readIndex));
      setValues(writeIndex, source);
    } else if (data instanceof LargeFloatData) {
      // Break apart the data source into its component values
      LargeFloatData large = (LargeFloatData) data;
      large.get(readIndex, this, writeIndex, length);
    } else if (data instanceof LargeDoubleData) {
      // Break apart the data source into its component values
      LargeDoubleData large = (LargeDoubleData) data;
      large.get(readIndex, this, writeIndex, length);
    } else if (data instanceof NumericData) {
      // General implementation that supports (naively) all other possible NumericData
      // implementations
      NumericData<?> bd = (NumericData<?>) data;
      for (long i = 0; i < length; i++) {
        setValue(writeIndex + i, bd.getValue(readIndex + i));
      }
    } else {
      throw new UnsupportedOperationException(
          "Cannot copy values from unsupported buffer: " + data);
    }
  }

  /**
   * Set the values of this NumericData to those in the given array `values`. Values are written
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
  public void setValues(long dataIndex, float[] values) {
    setValues(dataIndex, values, 0, values.length);
  }

  /**
   * Set the values of this NumericData to those in the given array `values`. Values are written
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
  public void setValues(long dataIndex, double[] values) {
    setValues(dataIndex, values, 0, values.length);
  }

  /**
   * Set the values of this NumericData to those in `values`. Values are written into this buffer
   * starting at `dataIndex` and read from `values` starting at `offset`. `length` floats are
   * taken from `values`.
   *
   * This is equivalent to calling {@link #setValue(long, double)} for every element of the array
   * with the appropriate offset into the data source. The float values are automatically widened to
   * `doubles`. The one distinction is that index access fails fast before anything is modified.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The source array providing the floats to this buffer
   * @param offset
   *     The index into the source array to start reading the floats
   * @param length
   *     The number of floats to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void setValues(long dataIndex, float[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      setValue(dataIndex + i, values[offset + i]);
    }
  }

  /**
   * Set the values of this NumericData to those in `values`. Values are written into this buffer
   * starting at `dataIndex` and read from `values` starting at `offset`. `length` doubles are
   * taken from `values`.
   *
   * This is equivalent to calling {@link #setValue(long, double)} for every element of the array
   * with the appropriate offset into this data source. The one distinction is that index access
   * fails fast before anything is modified.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The source array providing the doubles to this buffer
   * @param offset
   *     The index into the source array to start reading the doubles
   * @param length
   *     The number of doubles to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void setValues(long dataIndex, double[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      setValue(dataIndex + i, values[offset + i]);
    }
  }

  /**
   * Set the values of this NumericData to those into `values`. Data is written to this buffer
   * starting at `dataIndex` and read from the FloatBuffer starting at the FloatBuffer's position.
   * Floats are written from the buffer up to its configured limit. After invoking this method, the
   * buffer's position will be at its limit.
   *
   * This is equivalent to calling {@link #setValue(long, double)} for every element of the buffer
   * with the appropriate offset into the data source. The float values are automatically widened to
   * `doubles`. The one distinction is that index access fails fast before anything is modified.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The FloatBuffer source that provides floats from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of float to read (based on the remaining floats in `values`)
   *     would access bad elements
   */
  public void setValues(long dataIndex, FloatBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      setValue(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  /**
   * Set the values of this NumericData to those into `values`. Data is written to this buffer
   * starting at `dataIndex` and read from the DoubleBuffer starting at the DoubleBuffer's position.
   * Doubles are written from the buffer up to its configured limit. After invoking this method, the
   * buffer's position will be at its limit.
   *
   * This is equivalent to calling {@link #setValue(long, double)} for every element of the buffer
   * with the appropriate offset into this data source. The one distinction is that index access
   * fails fast before anything is modified.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the write
   * @param values
   *     The DoubleBuffer source that provides floats from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of double to read (based on the remaining doubles in
   *     `values`) would access bad elements
   */
  public void setValues(long dataIndex, DoubleBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      setValue(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  /**
   * Get the values of this NumericData and store them into the given array `values`.
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
  public void getValues(long dataIndex, float[] values) {
    getValues(dataIndex, values, 0, values.length);
  }

  /**
   * Get the values of this NumericData and store them into the given array `values`.
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
  public void getValues(long dataIndex, double[] values) {
    getValues(dataIndex, values, 0, values.length);
  }

  /**
   * Get the values of this NumericData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into `values` starting at `offset`. `length` floats are
   * read into `values`.
   *
   * This is equivalent to repeatedly calling {@link #getValue(long)}, then casting the `double`
   * to a `float`, and storing it into the array, with proper offsets for the data source and
   * the array. The one distinction is that index access fails fast before anything is modified.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The destination array receiving the floats from this buffer
   * @param offset
   *     The index into the destination array to start writing the floats
   * @param length
   *     The number of floats to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void getValues(long dataIndex, float[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = (float) getValue(dataIndex + i);
    }
  }

  /**
   * Get the values of this NumericData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into `values` starting at `offset`. `length` doubles are
   * read into `values`.
   *
   * This is equivalent to repeatedly calling {@link #getValue(long)} and then storing it into the
   * array, with proper offsets for the data source and the array. The one distinction is that index
   * access fails fast before anything is modified.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The destination array receiving the floats from this buffer
   * @param offset
   *     The index into the destination array to start writing the floats
   * @param length
   *     The number of doubles to copy
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and `length` would access bad elements of `this, or if `offset` and `length`
   *     access bad elements of `values`.
   */
  public void getValues(long dataIndex, double[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = getValue(dataIndex + i);
    }
  }

  /**
   * Get the values of this NumericData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into the FloatBuffer starting at the FloatBuffer's position.
   * Floats are read into the buffer up to its configured limit. After invoking this method, the
   * buffer's position will be at its limit.
   *
   * This is equivalent to repeatedly calling {@link #getValue(long)}, then casting the `double`
   * to a `float`, and storing it into the buffer, with proper offsets for the data source and
   * the array. The one distinction is that index access fails fast before anything is modified.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The ByteBuffer destination that receives floats from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of floats to read (based on the remaining floats in `values`)
   *     would access bad elements
   */
  public void getValues(long dataIndex, FloatBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, (float) getValue(dataIndex + i));
    }
    values.position(values.limit());
  }

  /**
   * Get the values of this NumericData and store them into `values`. Data is read from this buffer
   * starting at `dataIndex` and stored into the DoubleBuffer starting at the DoubleBuffer's
   * position. Doubles are read into the buffer up to its configured limit. After invoking this
   * method, the buffer's position will be at its limit.
   *
   * This is equivalent to repeatedly calling {@link #getValue(long)} and then storing it into the
   * buffer, with proper offsets for the data source and the array. The one distinction is that
   * index access fails fast before anything is modified.
   *
   * @param dataIndex
   *     The index into this buffer for the start of the read
   * @param values
   *     The ByteBuffer destination that receives doubles from this data source
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` and the number of doubles to read (based on the remaining doubles in
   *     `values`) would access bad elements
   */
  public void getValues(long dataIndex, DoubleBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, getValue(dataIndex + i));
    }
    values.position(values.limit());
  }
}
