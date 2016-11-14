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
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AbstractLargeDataBuffer
 * =======================
 *
 * Utility superclass for data buffers that store more elements than representable with an `int`
 * index. To do so efficiently, it groups other instances of the same type of DataBuffer and
 * requires that all buffers have the same length except the last, which must be less than or equal
 * to the size of the others.
 *
 * @author Michael Ludwig
 */
public class BufferConcatentation<S extends DataBuffer> implements DataView<List<S>> {
  /**
   * BulkOperation
   * =============
   *
   * Functional interface for operating on blocks of values that move between
   * two different array-like types.
   *
   * @param <S>
   *     The source array-like type
   * @param <D>
   *     The destination array-like type
   */
  @FunctionalInterface
  public interface BulkOperation<S, D> {
    /**
     * Move `length` values from `src` starting at `srcOffset` into `dst` starting at
     * `dstOffset`.
     *
     * @param src
     *     The source of values
     * @param srcOffset
     *     The offset into `src`
     * @param dst
     *     The destination of the values
     * @param dstOffset
     *     The offset into `dst`
     * @param length
     *     The number of values to copy
     */
    void run(S src, long srcOffset, D dst, int dstOffset, int length);
  }

  private final boolean bigEndian;
  private final long repeatedLength;
  private final S[] sources;
  private final long totalLength;

  /**
   * Create a new large data buffer that wraps the given ordered sub buffers. The overall state of
   * the data buffer is the concatenation of `sources` in the order provided. The length of
   * `sources` must be at least one. If `N` is the buffer length of the first DataBuffer in
   * `sources` and there are `M` buffers in `sources`, then buffers from `0` to `M-2` must have
   * length `N` and the buffer at `M-1` must have length less than or equal to `N`.
   *
   * All `M` data buffers must have the same byte order. The `sources` array is copied into an
   * internal array so future modifications to `sources` will not affect the created buffer. Changes
   * made to the buffers within `sources` will be reflected in this buffer.
   *
   * @param sources
   *     The data buffers to concatenate
   * @throws IllegalArgumentException
   *     if the sub-buffers' lengths do not meet the requirements described above
   */
  public BufferConcatentation(S[] sources) {
    this.sources = Arrays.copyOf(sources, sources.length);
    repeatedLength = sources[0].getLength();
    bigEndian = sources[0].isBigEndian();

    long total = repeatedLength;
    for (int i = 1; i < sources.length; i++) {
      if (i < sources.length - 1) {
        if (sources[i].getLength() != repeatedLength) {
          throw new IllegalArgumentException(
              "All but last source must have the same size, expected: " + repeatedLength
                  + ", but was " + sources[i].getLength());
        }
      } else {
        if (sources[i].getLength() > repeatedLength) {
          throw new IllegalArgumentException(
              "Last source can have at most size " + repeatedLength + ", but was " + sources[i]
                  .getLength());
        }
      }

      if (bigEndian != sources[i].isBigEndian()) {
        throw new IllegalArgumentException("Endianness of sources are not all the same");
      }
      total += sources[i].getLength();
    }
    totalLength = total;
  }

  /**
   * Perform an optimized bulk operation that splits executions of `op` across the sub-buffer data
   * boundaries as appropriate based on `dataIndex`, `offset` and `length`. This performs no data
   * validation. This uses the appropriate sub-buffer as the source to the BulkOperation and
   * `values` as the destination. However, the actual operation could in-fact move data the other
   * direction.
   *
   * @param op
   *     The bulk operation function to execute
   * @param dataIndex
   *     The logical start index into this large buffer
   * @param values
   *     The destination for the values
   * @param offset
   *     The offset into `values`
   * @param length
   *     The number of values to copy
   * @param <T>
   *     The type of the destination.
   */
  public <T> void bulkOperation(
      BulkOperation<S, T> op, long dataIndex, T values, int offset, int length) {
    long dataLength = dataIndex + length;
    while (dataIndex < dataLength) {
      // Get subsource and location within subsource for the copy
      S source = getSource(dataIndex);
      long inSourceIndex = getIndexInSource(dataIndex);

      // Get the number of elements from values that this subsource can receive
      long remainingSource = source.getLength() - inSourceIndex;
      int consumed = Math.toIntExact(Math.min(length, remainingSource));

      // Copy this new range into the subsource
      op.run(source, inSourceIndex, values, offset, consumed);

      // Update current index and range for the remainder of the values array
      dataIndex += consumed;
      offset += consumed;
      length -= consumed;
    }
  }

  /**
   * Copy values from this buffer concatenation into `dst` by splitting the logical range to copy
   * into sub blocks based on how the `srcIndex` and length overlap with the boundaries of the
   * sub-buffers of this instance. Data is moved into `dst` by calling {@link DataBuffer#set(long,
   * DataBuffer, long, long)} on `dst` for each sub-buffer that intersects the copy range, with
   * appropriate source and destination offsets and lengths.
   *
   * @param srcIndex
   *     The index into this buffer concatenation that is the start of the data to copy
   * @param dst
   *     The data buffer that receives the data
   * @param dstIndex
   *     The index that is the start of the received copy
   * @param length
   *     The number of elements to copy
   * @throws IndexOutOfBoundsException
   *     if bad indices would be accessed based on index and length
   */
  public void copyToDataBuffer(long srcIndex, DataBuffer dst, long dstIndex, long length) {
    Arguments.checkArrayRange("LargeData", getLength(), srcIndex, length);
    Arguments.checkArrayRange("DataBuffer", dst.getLength(), dstIndex, length);

    long dataLength = srcIndex + length;
    while (srcIndex < dataLength) {
      // Get subsource and location within subsource for the copy
      S source = getSource(srcIndex);
      long inSourceIndex = getIndexInSource(srcIndex);

      // Get the number of elements from values that this subsource can receive
      long remainingSource = source.getLength() - inSourceIndex;
      int consumed = Math.toIntExact(Math.min(length, remainingSource));

      // Copy values from source at srcIndex into dst at dstIndex
      dst.set(dstIndex, source, srcIndex, length);

      // Update current index and range for the remainder of the values array
      srcIndex += consumed;
      dstIndex += consumed;
      length -= consumed;
    }
  }

  /**
   * @param index
   *     The logical index for accessing data in the large buffer
   * @return The actual index within the appropriate sub-buffer to access the equivalent element
   */
  public long getIndexInSource(long index) {
    return index % repeatedLength;
  }

  /**
   * @return The total length of all concatenated sources
   */
  public long getLength() {
    return totalLength;
  }

  /**
   * @param index
   *     The logical index for accessing data in the large buffer
   * @return The sub-buffer that contains that logical index
   */
  public S getSource(long index) {
    return sources[(int) (index / repeatedLength)];
  }

  @Override
  public List<S> getSource() {
    return Collections.unmodifiableList(Arrays.asList(sources));
  }

  /**
   * Get the underlying data buffers that this large buffer concatenates. The returned array
   * is a defensive copy, although the actual DataBuffer elements are the not cloned.
   *
   * @return The sources of this large buffer
   */
  public S[] getSources() {
    return Arrays.copyOf(sources, sources.length);
  }

  /**
   * @return The endian-ness that all concatenated sources share
   */
  public boolean isBigEndian() {
    return bigEndian;
  }
}
